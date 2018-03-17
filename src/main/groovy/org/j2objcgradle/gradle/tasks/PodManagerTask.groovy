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
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.UnionFileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.j2objcgradle.gradle.BuildTypeProvider

class PodManagerTask extends DefaultTask{

    BuildContext _buildContext
    boolean testBuild
    String podfilePath

    static void addPodManagerTask(TaskContainer tasks, String path, BuildContext buildContext, boolean testBuild, Task downstream, Task upstream, int count)
    {
        Task task = tasks.create(name: "podManagerTask_${testBuild}_${count}", type: PodManagerTask){
            _buildContext = buildContext
            testBuild = true
            podfilePath = path
        }
        downstream.dependsOn(task)
        task.dependsOn(upstream)
    }

    static String getDependencyList(BuildContext _buildContext, boolean testBuild)
    {
        StringBuilder sb = new StringBuilder()

        appendDependencyNames(_buildContext.getDependencyResolver().translateJ2objcLibs, sb)
        if(testBuild)
            appendDependencyNames(_buildContext.getDependencyResolver().translateJ2objcTestLibs, sb)

        return sb.toString()
    }

    @Input
    boolean getJavaDebug()
    {
        return J2objcConfig.from(project).emitLineDirectives
    }

    @Input
    boolean getDependencyJavaDebug()
    {
        return J2objcConfig.from(project).dependenciesEmitLineDirectives
    }

    @Input
    String getDependencyList()
    {
        return getDependencyList(_buildContext, testBuild)
    }

    @Input
    String getInputFiles()
    {
        FileTree fileTree = new UnionFileTree("TranslateTask - ${(testBuild ? "test" : "main")}")

        BuildTypeProvider buildTypeProvider = _buildContext.getBuildTypeProvider()
        List<File> allFiles = new ArrayList<File>()

        for (FileTree tree : buildTypeProvider.sourceSets(project)) {
            fileTree.add(tree)
        }

        if(testBuild)
        {
            for (FileTree tree : buildTypeProvider.testSourceSets(project)) {
                fileTree.add(tree)
            }
        }

        J2objcConfig j2objcConfig = J2objcConfig.from(project)
        if(j2objcConfig.translatePattern != null) {
            fileTree = fileTree.matching(j2objcConfig.translatePattern)
        }

        fileTree = fileTree.matching(TranslateTask.javaPattern {
            include "**/*.java"
        })

        allFiles.addAll(fileTree.getFiles())
        Collections.sort(allFiles)

        return allFiles.join(File.pathSeparator)
    }

    @TaskAction
    void rebuildPod(IncrementalTaskInputs inputs) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        ByteArrayOutputStream stderr = new ByteArrayOutputStream()

        try {
            Utils.projectExec(project, stdout, stderr, null, {
                executable "pod"

                args "install"

                setStandardOutput stdout
                setErrorOutput stderr

                setWorkingDir podfilePath
            })
        } catch (Exception exception) {  // NOSONAR
            // TODO: match on common failures and provide useful help
            throw exception
            stderr.close()
            stdout.close()
            if(stdout.size() > 0)
            {
                println("********* STDOUT *********")
                println new String(stdout.toByteArray())
            }
            if(stderr.size() > 0)
            {
                println("********* STDERR *********")
                println new String(stderr.toByteArray())
            }
        }
    }

    private static void appendDependencyNames(ArrayList<J2objcDependency> libs, StringBuilder sb) {
        for (J2objcDependency dependency : libs) {
            sb.append(dependency.dependencyFolderLocation().getName()).append("|")
        }
    }
}
