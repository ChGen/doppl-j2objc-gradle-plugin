/*
 * Copyright (c) 2017 Touchlab Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.j2objcgradle.gradle.tasks

import groovy.transform.CompileStatic
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.internal.file.UnionFileTree
import org.gradle.api.tasks.*
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil
import org.j2objcgradle.gradle.DependencyResolver
import org.j2objcgradle.gradle.J2objcConfig
import org.j2objcgradle.gradle.J2objcInfo
import org.j2objcgradle.gradle.J2objcVersionManager

/**
 * Translation task for Java to Objective-C using j2objc tool.
 */
@CompileStatic
class TranslateTask extends BaseChangesTask {

    boolean testBuild

    @Input
    def outBaseName

    @InputFile
    File dependencyMappings

    @OutputFile
    File getOutputMapping() {
        project.file("${project.buildDir}/j2objcBuild/${outBaseName}.mapping")
    }

    List<FileTree> inputSourceSets = []

    def getFileName() {
        "${outBaseName}SourceOut"
    }

    String getBaseDir() {
        "$project.buildDir/j2objcBuild/translated/$outBaseName"
    }

    @OutputFile
    File getHeader() {
        new File(baseDir, "${fileName}.h")
    }

    @OutputFile
    File getImplementation() {
        new File(baseDir, "${fileName}.m")
    }

    List<DependencyResolver> resolvers = []

    @InputFiles
    Set<File> getDependencyJavaFoldersAsFiles() {
        Set<File> fs = []
        resolvers.each {
            it.dependencyJavaDirs.each {
                fs.add(it)
            }
        }
        return fs
    }

    @InputFiles
    FileCollection getDependencyJavaFoldersAsFileCollection() {
        UnionFileCollection union = new UnionFileCollection()
        resolvers.each {
            it.dependencyJavaDirs.each {
                union.add(project.files(it))
            }
        }
        return union
    }

    def dependencies(DependencyResolver dependencyResolver) {
        dependsOn(dependencyResolver)
        resolvers.add(dependencyResolver)
        inputFiles dependencyResolver.dependencyJavaDirs
    }

    def outBaseName(String outBaseName) {
        this.outBaseName = outBaseName
    }

    def inputFileTrees(Collection<FileTree> sets) {
        inputSourceSets.addAll(sets)
    }

    def inputFiles(Collection<File> sets) {
        sets.each {
            inputSourceSets.add(project.fileTree(it))
        }
    }

    @InputFiles
    Set<File> getAllJava() {
        FileTree fileTree = new UnionFileTree()
        for (FileTree set : inputSourceSets) {
            fileTree.add(set)
        }

        J2objcConfig j2objcConfig = J2objcConfig.from(project)
        if(j2objcConfig.translatePattern != null) {
            fileTree = fileTree.matching(j2objcConfig.translatePattern)
        }

        fileTree = fileTree.matching(javaPattern {
            include "**/*.java"
        })

        return fileTree.files
    }

    static PatternSet javaPattern(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PatternSet) Closure cl) {
        PatternSet translatePattern = new PatternSet()
        return ConfigureUtil.configure(cl, translatePattern)
    }

    @Input boolean isEmitLineDirectives() {
        J2objcConfig.from(project).emitLineDirectives
    }

    @InputDirectory @Optional
    File getObjcDir(){
        File f = testBuild ? project.file(J2objcInfo.SOURCEPATH_OBJC_TEST) : project.file(J2objcInfo.SOURCEPATH_OBJC_MAIN)
        return f.exists() ? f : null
    }

    @TaskAction
    void translate(IncrementalTaskInputs inputs) {

        J2objcConfig j2objcConfig = J2objcConfig.from(project)
        if(testBuild && j2objcConfig.skipTests)
            return

        J2objcVersionManager.checkJ2objcConfig(project, true)

        File objcDir = getObjcDir()
        if(objcDir != null)
        {
            Utils.projectCopy(project, {
                from objcDir
                into baseDir
                includeEmptyDirs = false
            })
        }

        List<String> translateArgs = getTranslateArgs()

        def prefixMap = getPrefixes()

        doTranslate(
                getAllJavaFolders(),
                translateArgs,
                prefixMap,
                isEmitLineDirectives()
        )

        if (prefixMap.size() > 0) {
            def prefixes = new File(project.buildDir, "prefixes.properties")
            def writer = new FileWriter(prefixes)

            Utils.propsFromStringMap(prefixMap).store(writer, null);

            writer.close()
        }
    }

    @InputFiles
    Set<File> getAllJavaFolders() {
        Set<File> allFiles = new HashSet<>()
        inputSourceSets.each {
            allFiles.add(((ConfigurableFileTree)it).dir)
        }
        return allFiles
    }


    void recursiveGrab(File dir, List<File> files) {
        if(dir.isDirectory())
        {
            File[] dirFiles = dir.listFiles()
            for (File f : dirFiles) {
                if(f.isDirectory())
                    recursiveGrab(f, files)
                else if(f.getName().endsWith(".java"))
                    files.add(f);
            }
        }
    }

    void doTranslate(
            Collection<File> sourcepathDirs,
            List<String> translateArgs,
            Map<String, String> prefixMap,
            boolean emitLineDirectives) {

        Set<File> allTranslateFiles = getAllJava()

        if(allTranslateFiles.size() == 0)
            return

        J2objcInfo j2objcInfo = J2objcInfo.getInstance(project)
        String j2objcExecutable = "${getJ2objcHome()}/j2objc"

        sourcepathDirs.addAll(dependencyJavaFoldersAsFiles)
        String sourcepathArg = Utils.joinedPathArg(sourcepathDirs)

        //Classpath arg for translation. Includes user specified jars, j2objc 'standard' jars, and j2objc dependency libs
        UnionFileCollection classpathFiles = new UnionFileCollection([
                project.files(Utils.j2objcLibs(getJ2objcHome(), getTranslateJ2objcLibs()))
        ])

        String classpathArg = Utils.joinedPathArg(classpathFiles)

        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        ByteArrayOutputStream stderr = new ByteArrayOutputStream()

        List<String> mappingFiles = new ArrayList<>()
        Map<String, String> allPrefixes = new HashMap<>(prefixMap)

        addMappings(dependencyMappings, mappingFiles)

        if(testBuild)
        {
            addMappings(j2objcInfo.dependencyOutTestMappings(), mappingFiles)
            addMappings(j2objcInfo.sourceBuildOutMainMappings(), mappingFiles)
        }

        File javaBatch = new File(baseDir, "javabatch.in")
        javaBatch.write(allTranslateFiles.join("\n"))

        try {
            Utils.projectExec(project, stdout, stderr, null, {
                executable j2objcExecutable

                args "-d", Utils.relativePath(project.projectDir, project.file(baseDir))
                args "-XcombineJars", ''
                args "-XglobalCombinedOutput", fileName
                args "--swift-friendly", ''
                args "--output-header-mapping", outputMapping.path

                if(emitLineDirectives)
                {
                    args "-g", ''
                }

                if (mappingFiles.size() > 0) {
                    args "--header-mapping", mappingFiles.join(",")
                }

                args "-sourcepath", sourcepathArg

                if(!classpathArg.isEmpty()) {
                    args "-classpath", classpathArg
                }

                translateArgs.each { String translateArg ->
                    args translateArg
                }

                allPrefixes.keySet().each { String packageString ->
                    args "--prefix", packageString + "=" + allPrefixes.get(packageString)
                }

                args "@${Utils.relativePath(project.projectDir, javaBatch)}"

                setStandardOutput stdout
                setErrorOutput stderr

                setWorkingDir project.projectDir
            })

        } catch (Exception exception) {  // NOSONAR
            // TODO: match on common failures and provide useful help
            throw exception
        }

//        javaBatch.delete()
    }

    private void addMappings(File mapFile, List<String> mappingFiles) {
        if(mapFile.exists())
            mappingFiles.add(mapFile.path)
    }

}
