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

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.internal.file.UnionFileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.j2objcgradle.gradle.DependencyResolver
import org.j2objcgradle.gradle.J2objcConfig
import org.j2objcgradle.gradle.J2objcInfo

class TranslateDependenciesTask extends BaseTranslateTask {

    boolean testBuild

    Set<DependencyResolver> resolvers = []

    @Input
    def outBaseName

    def getFileName() {
        "${outBaseName}DependenciesOut"
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


    @InputFiles
    FileCollection dependencyJavaFoldersAsFileCollection() {
        UnionFileCollection inputs = new UnionFileCollection()
        resolvers
                .collect({it.dependencyJavaDirs})
                .each { inputs.add(project.files(it)) }
        inputs
    }

    @InputFiles
    Set<File> dependencyJavaFoldersAsFiles() {
        new HashSet<>(resolvers
                .collect({it.dependencyJavaDirs})
                .flatten())
    }

    void dependencies(DependencyResolver... dependencyResolvers) {
        dependencyResolvers.each {
            dependsOn(it)
            resolvers.add(it)
        }
    }

    @Input boolean isDependenciesEmitLineDirectives() {
        J2objcConfig.from(project).dependenciesEmitLineDirectives
    }

    @OutputFile
    File getOutputMapping() {
        project.file("${project.buildDir}/j2objcBuild/${outBaseName}Dependency.mapping")
    }

    @TaskAction
    void translateDependencies(IncrementalTaskInputs inputs) {

        outputMapping.createNewFile()

        String j2objcExecutable = "${getJ2objcHome()}/j2objc"

        J2objcInfo j2objcInfo = J2objcInfo.getInstance(project)

        List<File> sourcepathList = new ArrayList<>()

        if (!dependencyJavaFoldersAsFiles().empty) {
            dependencyJavaFoldersAsFiles().each {
                sourcepathList.add(it)
            }
        }
        Map<String, String> allPrefixes = getPrefixes()

        runTranslate(j2objcExecutable, j2objcInfo, sourcepathList, allPrefixes)
    }

    private void runTranslate(String j2objcExecutable, J2objcInfo j2objcInfo, List<File> sourcepathList,
                              allPrefixes) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        ByteArrayOutputStream stderr = new ByteArrayOutputStream()

        UnionFileTree fileTree = new UnionFileTree("All Dependency Java")

        fileTree.add(dependencyJavaFoldersAsFileCollection().asFileTree.matching { include "**/*.java" })

        def files = fileTree.files
        if (files.size() == 0) {
            return
        }


        File javaBatch = new File(baseDir, "javabatch.in")
        javaBatch.write(files.join("\n"))

        try {
            Utils.projectExec(project, stdout, stderr, null, {
                executable j2objcExecutable

                // Arguments
                args "-d", Utils.relativePath(project.projectDir, project.file(baseDir))
                args "-XcombineJars", ''
                args "-XglobalCombinedOutput", fileName
                args "--swift-friendly", ''
                args "--output-header-mapping", outputMapping.path

                if (isDependenciesEmitLineDirectives()) {
                    args "-g", ''
                }

                if (sourcepathList.size() > 0) {
                    args "-sourcepath", Utils.joinedPathArg(sourcepathList)
                }

                if (testBuild) {
                    File mappingsFile = j2objcInfo.sourceBuildOutMainMappings()
                    if (mappingsFile.exists()) {
                        args "--header-mapping", mappingsFile.path
                    }
                }

                configureClasspathArg it

                getTranslateArgs().each { String translateArg ->
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

            javaBatch.delete()

        } catch (Exception exception) {  // NOSONAR
            // TODO: match on common failures and provide useful help
            throw exception
        }


    }
}
