

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

package org.j2objcgradle.gradle

import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTree
import org.j2objcgradle.gradle.tasks.Utils

/**
 * Created by kgalligan on 3/11/17.
 */
class AndroidBuildTypeProvider implements BuildTypeProvider{
    def variants = null
    private J2objcConfig j2objcConfig

    AndroidBuildTypeProvider(Project project) {
        j2objcConfig = J2objcConfig.from(project)
        if (project.plugins.findPlugin("com.android.application") ||
            project.plugins.findPlugin("android") ||
            project.plugins.findPlugin("com.android.test")) {
            variants = "applicationVariants";
        } else if (project.plugins.findPlugin("com.android.library") ||
                   project.plugins.findPlugin("android-library") ||
                   project.plugins.findPlugin("com.android.feature")) {
            variants = "libraryVariants";
        } else {
            throw new ProjectConfigurationException("The android or android-library plugin must be applied to the project", null)
        }
    }

    @Override
    void configureDependsOn(Project project, Task upstreamTask, Task downstreamTask) {
        insertTaskDependency(project, "assemble${j2objcConfig.targetVariant.capitalize()}", upstreamTask, downstreamTask)
    }

    @Override
    void configureTestDependsOn(Project project, Task upstreamTask, Task downstreamTask) {
        insertTaskDependency(project, "test${j2objcConfig.targetVariant.capitalize()}UnitTest", upstreamTask, downstreamTask)
    }

    private void insertTaskDependency(Project project, String taskName, Task upstreamTask, Task downstreamTask) {
        Task foundTask = project.tasks.getByName(taskName)
        foundTask.dependsOn upstreamTask
        downstreamTask.dependsOn foundTask
    }

    @Override
    List<FileTree> testSourceSets(Project project) {
        return sourceSets(project, true)
    }

    @Override
    List<FileTree> sourceSets(Project project) {
        return sourceSets(project, false)
    }

    List<FileTree> sourceSets(Project project, boolean test) {

        List<FileTree> paths = new ArrayList<>()

        boolean variantFound = false

        project.android[variants].all { variant ->
            if(variant.getName() == j2objcConfig.targetVariant) {
                variantFound = true
                List<FileTree> javaSources = test ? variant.unitTestVariant.variantData.javaSources : variant.variantData.javaSources
                for (FileTree s : javaSources) {
                    String path = Utils.dirFromFileTree(s).getPath()
                    if(!path.contains("/rs/") && !path.contains("/aidl/"))
                        paths.add(s)
                }
                List<File> generatedSourceFolders = test ? variant.unitTestVariant.variantData.extraGeneratedSourceFolders : variant.variantData.extraGeneratedSourceFolders
                if(generatedSourceFolders != null)
                {
                    for (File file : generatedSourceFolders) {
                        paths.add(project.fileTree(dir: file, includes: ["**/*.java"]))
                    }
                }
            }
        }

        if(!variantFound)
        {
            throw new ProjectConfigurationException("Variant ${j2objcConfig.targetVariant} not found for project. Please set j2objcConfig.targetVariant.", null)
        }

        ArrayList<String> generatedSourceDirs = test ? j2objcConfig.generatedTestSourceDirs : j2objcConfig.generatedSourceDirs

        if(generatedSourceDirs.size() > 0)
        {
            List<ConfigurableFileTree> trees = Utils.javaTrees(project, generatedSourceDirs)
            for (ConfigurableFileTree tree : trees) {
                paths.add(tree)
            }
        }

        //annotationProcessor doesn't show up in collection of added source directories (or I haven't found it)
        if(test) {
            Configuration testAnnotationProcessorConfig = project.configurations.findByName("testAnnotationProcessor")
            if (testAnnotationProcessorConfig != null) {
                paths.add(project.fileTree('build/generated/source/apt/test/' + j2objcConfig.targetVariant)) //TODO: gradle-5.4.1
            }
        }else{
            Configuration annotationProcessorConfig = project.configurations.findByName("annotationProcessor")
            if(annotationProcessorConfig != null)
            {
                //path before gradle-5.4.1
                //paths.add(project.fileTree('build/generated/source/apt/'+ j2objcConfig.targetVariant))
                //path since gradle-5.4.1
                paths.add(project.fileTree('build/generated/ap_generated_sources/'+ j2objcConfig.targetVariant))
            }
        }

        return paths
    }
}
