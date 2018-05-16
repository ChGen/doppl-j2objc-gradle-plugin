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

import org.j2objcgradle.gradle.BuildContext
import org.j2objcgradle.gradle.J2objcConfig
import org.j2objcgradle.gradle.J2objcDependency
import org.j2objcgradle.gradle.J2objcInfo
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction
import org.j2objcgradle.gradle.DependencyResolver
import org.j2objcgradle.gradle.FrameworkConfig

class FrameworkTask extends DefaultTask {

    public boolean test
    BuildContext _buildContext

    static File podspecFile(Project project, boolean test)
    {
        return configFile(project, test, "podspec")
    }

    static File headerFile(Project project, boolean test)
    {
        String specName = podspecName(project, test)
        File podspecFile = new File(project.buildDir, "${specName}.h")
        return podspecFile
    }

    static File configFile(Project project, boolean test, String extension)
    {
        String specName = podspecName(project, test)
        File podspecFile = new File(project.projectDir, "${specName}.${extension}")
        return podspecFile
    }

    static String podspecName(Project project, boolean test) {
        def spec = J2objcConfig.from(project)
        test ? "${spec.podName}Test" : spec.podName
    }

    private List<J2objcDependency> dependencyList(boolean testBuild) {
        DependencyResolver resolver = _buildContext.getDependencyResolver()
        return testBuild ? resolver.translateJ2objcTestLibs : resolver.translateJ2objcLibs
    }

    List<File> getJavaSourceFolders(boolean testBuild)
    {
        List<File> allFiles = new ArrayList<>()

        if(testBuild){
            allFiles.addAll(TranslateTask.testSourceDirs(project, _buildContext))
        }else{
            allFiles.addAll(TranslateTask.mainSourceDirs(project, _buildContext))
        }

        Collections.sort(allFiles)

        return allFiles
    }

    @TaskAction
    public void writePodspec() {
        def j2objcConfig = J2objcConfig.from(project)
        if(test && j2objcConfig.skipTests)
            return

        String specName = podspecName(project, test)
        File podspecFile = podspecFile(project, test)

        List<File> objcFolders = new ArrayList<>()
        List<File> headerFolders = new ArrayList<>()
        List<File> srcHeaderFolders = new ArrayList<>()
        List<File> javaFolders = new ArrayList<>()

        J2objcInfo j2objcInfo = J2objcInfo.getInstance(project)

        if(j2objcConfig.emitLineDirectives)
            javaFolders.addAll(getJavaSourceFolders(false))

        objcFolders.add(j2objcInfo.dependencyOutFileMain())
        objcFolders.add(j2objcInfo.sourceBuildOutFileMain())
        headerFolders.add(j2objcInfo.dependencyOutFileMain())
        headerFolders.add(j2objcInfo.sourceBuildOutFileMain())

        fillDependenciesFromList(dependencyList(false), objcFolders, srcHeaderFolders, j2objcConfig.dependenciesEmitLineDirectives ? javaFolders : null)

        if(test)
        {
            if(j2objcConfig.emitLineDirectives)
                javaFolders.addAll(getJavaSourceFolders(true))

            objcFolders.add(j2objcInfo.dependencyOutFileTest())
            objcFolders.add(j2objcInfo.sourceBuildOutFileTest())
            headerFolders.add(j2objcInfo.dependencyOutFileTest())
            headerFolders.add(j2objcInfo.sourceBuildOutFileTest())

            fillDependenciesFromList(dependencyList(true), objcFolders, srcHeaderFolders, j2objcConfig.dependenciesEmitLineDirectives ? javaFolders : null)
        }

        FrameworkConfig config = test ? FrameworkConfig.findTest(project) : FrameworkConfig.findMain(project)

        File headerFile = headerFile(project, test)

        String podspecTemplate = config.podspecTemplate(
                project,
                headerFile,
                objcFolders,
                headerFolders,
                srcHeaderFolders,
                javaFolders,
                specName)

        BufferedWriter writer = null

        if(headerFile.exists())
            headerFile.delete()

        BufferedWriter headerWriter = new BufferedWriter(new FileWriter(headerFile))
        try {
            writer = new BufferedWriter(new FileWriter(podspecFile))
            writer.write(podspecTemplate.toString())
            for (File folder : headerFolders) {
                FileTree fileTree = project.fileTree(dir: folder, includes: ["**/*.h"])
                Set<File> files = fileTree.files
                for (File f : files) {
                    headerWriter.append("#include \"${f.getName()}\"\n")
                }
            }
        } finally {
            if (writer != null)
                writer.close()
            if(headerWriter != null)
                headerWriter.close()
        }
    }

    private void addSourceLinks(boolean testBuild, ArrayList<File> javaFolders) {
        List<File> sourceFolders = getJavaSourceFolders(testBuild)
        javaFolders.addAll(sourceFolders)
    }

    private void fillDependenciesFromList(List<J2objcDependency> mainDependencies,
                                          ArrayList<File> objcFolders,
                                          ArrayList<File> headerFolders,
                                          ArrayList<File> javaFolders) {
        for (J2objcDependency dep : mainDependencies) {
            File sourceFolder = new File(dep.dependencyFolderLocation(), "src")
            if (sourceFolder.exists()) {
                objcFolders.add(sourceFolder)
                headerFolders.add(sourceFolder)
            }
            if (javaFolders != null && dep.dependencyJavaFolder().exists()) {
                javaFolders.add(dep.dependencyJavaFolder())
            }
        }
    }
}
