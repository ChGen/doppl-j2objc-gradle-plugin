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
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.TaskAction

/**
 * Resolves j2objc dependencies. Can handle external artifacts as well as project dependencies
 */
@CompileStatic
class DependencyResolver extends DefaultTask{

    //Legacy gradle configs
    public static final String CONFIG_DOPPL = 'doppl'
    public static final String CONFIG_DOPPL_ONLY = 'dopplOnly'
    public static final String CONFIG_TEST_DOPPL = 'testDoppl'

    //Main gradle configs
    public static final String CONFIG_J2OBJC = 'j2objc'
    public static final String CONFIG_J2OBJC_ONLY = 'j2objcOnly'
    public static final String CONFIG_TEST_J2OBJC = 'testJ2objc'

    List<J2objcDependency> translateJ2objcLibs = new ArrayList<>()
    List<J2objcDependency> translateJ2objcTestLibs = new ArrayList<>()

    @TaskAction
    void inflateAll()
    {
        configureAll()
        for (J2objcDependency dep : translateJ2objcLibs) {
            dep.expandDop(project)
        }
        for (J2objcDependency dep : translateJ2objcTestLibs) {
            dep.expandDop(project)
        }
    }

    void configureAll() {
        J2objcInfo j2objcInfo = J2objcInfo.getInstance(project)

        Map<String, J2objcDependency> dependencyMap = new HashMap<>()

        configForConfig(CONFIG_DOPPL, translateJ2objcLibs, j2objcInfo.dependencyExplodedJ2objcFile(), dependencyMap)
        configForConfig(CONFIG_J2OBJC, translateJ2objcLibs, j2objcInfo.dependencyExplodedJ2objcFile(), dependencyMap)
        configForConfig(CONFIG_DOPPL_ONLY, translateJ2objcLibs, j2objcInfo.dependencyExplodedJ2objcOnlyFile(), dependencyMap)
        configForConfig(CONFIG_J2OBJC_ONLY, translateJ2objcLibs, j2objcInfo.dependencyExplodedJ2objcOnlyFile(), dependencyMap)
        configForConfig(CONFIG_TEST_DOPPL, translateJ2objcTestLibs, j2objcInfo.dependencyExplodedTestJ2objcFile(), dependencyMap)
        configForConfig(CONFIG_TEST_J2OBJC, translateJ2objcTestLibs, j2objcInfo.dependencyExplodedTestJ2objcFile(), dependencyMap)
    }

    void configForConfig(String configName,
                         List<J2objcDependency> dependencyList,
                         File explodedPath,
                         Map<String, J2objcDependency> dependencyMap){
        Project localProject = project
        configForProject(localProject, configName, dependencyList, dependencyMap, explodedPath)
    }

    private void configForProject(Project localProject,
                                  String configName,
                                  List<J2objcDependency> dependencyList,
                                  Map<String, J2objcDependency> dependencyMap,
                                  File explodedPath) {
        def dependencyConfig = localProject.configurations.getByName(configName)

        //Add project dependencies
        dependencyConfig.dependencies.each {
            if (it instanceof ProjectDependency) {

                Project beforeProject = it.dependencyProject
                String projectDependencyKey = beforeProject.getPath()
                if(!dependencyMap.containsKey(projectDependencyKey)) {
                    J2objcDependency dependency = new J2objcDependency(
                            beforeProject.name,
                            new File(beforeProject.projectDir, "src/main")
                    )

                    dependencyList.add(
                            dependency
                    )

                    dependencyMap.put(projectDependencyKey, dependency)
                    configForProject(beforeProject, configName, dependencyList, dependencyMap, explodedPath)
                }
            }
        }

        //Add external "dop" file dependencies
        dependencyConfig.resolvedConfiguration.resolvedArtifacts.each { ResolvedArtifact ra ->

            def extension = ra.extension
            def classifier = ra.classifier
            if ((extension != null && extension.equals("dop"))
                    ||
                (classifier != null && classifier.equals("sources")))
              {
                def group = ra.moduleVersion.id.group
                def name = ra.moduleVersion.id.name
                def version = ra.moduleVersion.id.version

                String mapKey = group + "_" + name + "_" + version
                if (!dependencyMap.containsKey(mapKey)) {

                    def dependency = new J2objcDependency(
                            group,
                            name,
                            version,
                            explodedPath,
                            ra.file
                    )

                    dependencyList.add(dependency)
                    dependencyMap.put(mapKey, dependency)
                }
            }
        }
    }


}
