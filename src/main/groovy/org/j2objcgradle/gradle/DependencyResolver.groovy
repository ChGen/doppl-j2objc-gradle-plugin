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

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectCollection
import org.gradle.api.DomainObjectSet
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

/**
 * Resolves j2objc dependencies. Can handle external artifacts as well as project dependencies
 */
@CompileStatic
class DependencyResolver extends DefaultTask {

    @Nested
    Map<String, J2objcDependency> dependencyMap = new HashMap<>()

    void forConfiguration(String... configs) {
        for (String config : configs) {
            configForConfig(config)
        }
    }

    @OutputDirectory
    File destinationDir = new File("${project.buildDir}/j2objcBuild/dependencies/exploded/${basename}")

    @OutputDirectories
    DefaultDomainObjectSet<File> dependencyJavaDirs = new DefaultDomainObjectSet<>(File.class)

    @OutputDirectories
    DefaultDomainObjectSet<File> dependencyNativeDirs = new DefaultDomainObjectSet<>(File.class)

    def getBasename() {
        "${name.replaceAll(J2objcPlugin.J2OBJC_DEPENDENCY_RESOLVER, "")}"
    }

    @TaskAction
    void inflateAll() {
        dependencyMap.values().each {
            it.expandDop(project)

            if (it.dependencyNativeFolder().exists()) {
                dependencyNativeDirs.add(it.dependencyNativeFolder())
            }
        }
    }

    void configForConfig(String configName){
        configForProject(project, configName)
    }

    private void configForProject(Project localProject,
                                  String configName) {
        Configuration dependencyConfig = localProject.configurations.getByName(configName)

        //Add project dependencies
        dependencyConfig.dependencies.all {
            if (it instanceof ProjectDependency) {

                Project beforeProject = it.dependencyProject
                String projectDependencyKey = beforeProject.getPath()
                if(!dependencyMap.containsKey(projectDependencyKey)) {
                    J2objcDependency dependency = new J2objcDependency(
                            beforeProject.name,
                            new File(beforeProject.projectDir, "src/main")
                    )

                    dependencyMap.put(projectDependencyKey, dependency)
                    configForProject(beforeProject, configName)
                }
            }
        }

        project.afterEvaluate {
            dependencyConfig.resolvedConfiguration.resolvedArtifacts.each {

                def extension = it.extension
                def classifier = it.classifier
                if ("dop".equals(extension) || "sources".equals(classifier)) {

                    def group = it.moduleVersion.id.group
                    def name = it.moduleVersion.id.name
                    def version = it.moduleVersion.id.version

                    String mapKey = group + "_" + name + "_" + version
                    if (!dependencyMap.containsKey(mapKey)) {

                        def dependency = new J2objcDependency(
                                group,
                                name,
                                version,
                                destinationDir,
                                it.file
                        )

                        dependencyMap.put(mapKey, dependency)

                        dependencyJavaDirs.add(dependency.dependencyJavaFolder())
                    }
                }
            }
        }
    }

}
