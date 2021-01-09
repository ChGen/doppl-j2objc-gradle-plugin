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

import org.j2objcgradle.gradle.tasks.CleanJ2objcRuntimeTask
import org.j2objcgradle.gradle.tasks.CycleFinderTask
import org.j2objcgradle.gradle.tasks.J2objcAssemblyTask
import org.j2objcgradle.gradle.tasks.FrameworkTask
import org.j2objcgradle.gradle.tasks.PodManagerTask
import org.j2objcgradle.gradle.tasks.TestTranslateTask
import org.j2objcgradle.gradle.tasks.TranslateDependenciesTask
import org.j2objcgradle.gradle.tasks.TranslateTask
import org.j2objcgradle.gradle.tasks.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar

/*
 * Main plugin class for creation of extension object and all the tasks.
 */
class J2objcPlugin implements Plugin<Project> {

    public static final String TASK_TEST_CLASS_LISTING = 'j2objcTestClassListing'

    public static final String TASK_J2OBJC_ASSEMBLY = 'j2objcAssembly'
    public static final String TASK_J2OBJC_ARCHIVE = 'j2objcArchive'

    public static final String TASK_J2OBJC_DEPENDENCY_TRANSLATE_MAIN = 'j2objcDependencyTranslateMain'
    public static final String TASK_J2OBJC_DEPENDENCY_TRANSLATE_TEST = 'j2objcDependencyTranslateTest'
    public static final String TASK_J2OBJC_MAIN_TRANSLATE = 'j2objcMainTranslate'
    public static final String TASK_J2OBJC_TEST_TRANSLATE = 'j2objcTestTranslate'
    public static final String TASK_J2OBJC_CLEAN_RUNTIME = 'j2objcCleanRuntime'
    public static final String TASK_J2OBJC_FRAMEWORK_MAIN = 'j2objcFrameworkMain'
    public static final String TASK_J2OBJC_FRAMEWORK_TEST = 'j2objcFrameworkTest'
    public static final String TASK_J2OBJC_BUILD = 'j2objcBuild'

    public static final String TASK_J2OBJC_CYCLE_FINDER = 'j2objcCycleFinder'

    public static final String J2OBJC_DEPENDENCY_RESOLVER = 'j2objcDependencyResolver'

    public static final String TASK_J2OBJC_PRE_BUILD = 'j2objcPreBuild'
    public static final String TASK_J2OBJC_CONTEXT_BUILD = 'j2objcContextBuild'

    @Override
    void apply(Project project) {

        boolean javaTypeProject = Utils.isJavaTypeProject(project);

        boolean androidTypeProject = Utils.isAndroidTypeProject(project);

        if(!javaTypeProject && !androidTypeProject) {
            throw new ProjectConfigurationException("J2objc gradle plugin depends on running java or one of the Android gradle plugins. None of those were found. If you have one, please make sure to apply j2objc AFTER the other plugin(s)", null)
        }

        project.with {

            J2objcInfo j2objcInfo = J2objcInfo.getInstance(project)
            extensions.create('j2objcConfig', J2objcConfig, project)

            extensions.j2objcConfig.extensions.create('mainFramework', FrameworkConfig, false)
            extensions.j2objcConfig.extensions.create('testFramework', FrameworkConfig, true)

            // These configurations are groups of artifacts and dependencies for the plugin build
            // https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.Configuration.html
            // We're keeping legacy 'doppl' configs because serveral projects use them and would require modification.
            configurations {
                j2objc{
                    transitive = true
                    description = 'For j2objc special packages'
                }
                doppl{
                    transitive = true
                    description = 'For doppl special packages'
                }
                j2objcOnly{
                    transitive = true
                    description = 'For j2objc special packages, do not include in dependencies'
                }
                dopplOnly{
                    transitive = true
                    description = 'For doppl special packages, do not include in dependencies'
                }
                testJ2objc{
                    transitive = true
                    description = 'For j2objc testing special packages'
                }
                testDoppl{
                    transitive = true
                    description = 'For doppl testing special packages'
                }
            }

            if(!J2objcConfig.from(project).skipDefaultDependencies) {
                dependencies {
                    if (javaTypeProject) {
                        J2objcVersionManager.checkJ2objcConfig(project, true)
                        compileOnly project.files(Utils.j2objcHome(project) + "/lib/jre_emul.jar")
                        testCompile project.files(Utils.j2objcHome(project) + "/lib/jre_emul.jar")
                    }

                    implementation 'com.google.j2objc:j2objc-annotations:1.3'

                    compileOnly 'com.google.code.findbugs:jsr305:3.0.2'
                    testImplementation 'com.google.code.findbugs:jsr305:3.0.2'
                    j2objcOnly 'com.google.code.findbugs:jsr305:3.0.2:sources'

                    compileOnly 'javax.inject:javax.inject:1'
                    testImplementation 'javax.inject:javax.inject:1'
                    j2objcOnly 'javax.inject:javax.inject:1:sources'
                }
            }

            // Produces a modest amount of output
            logging.captureStandardOutput LogLevel.INFO

            // If users need to generate extra files that j2objc depends on, they can make this task dependent
            // on such generation.

            DependencyResolver dependencyResolver = (DependencyResolver)tasks.create(name: J2OBJC_DEPENDENCY_RESOLVER, type: DependencyResolver)

            BuildContext buildContext = new BuildContext(project, dependencyResolver)

            Task j2objcPreBuildTask = tasks.create(name: TASK_J2OBJC_PRE_BUILD, type: DefaultTask, dependsOn: J2OBJC_DEPENDENCY_RESOLVER) {
                group 'j2objc'
                description "Marker task for all tasks that must be complete before j2objc building"
            }

            Task j2objcContextBuildTask = tasks.create(name: TASK_J2OBJC_CONTEXT_BUILD, type: DefaultTask, dependsOn: TASK_J2OBJC_PRE_BUILD) {
                group 'j2objc'
                description "Marker task for all tasks after the underlying Java build system runs"
            }

            tasks.create(name: TASK_J2OBJC_DEPENDENCY_TRANSLATE_MAIN, type: TranslateDependenciesTask, dependsOn: TASK_J2OBJC_CONTEXT_BUILD){
                _buildContext = buildContext
                testBuild = false
            }

            tasks.create(name: TASK_J2OBJC_DEPENDENCY_TRANSLATE_TEST, type: TranslateDependenciesTask, dependsOn: TASK_J2OBJC_CONTEXT_BUILD){
                _buildContext = buildContext
                testBuild = true
            }

            tasks.create(name: TASK_J2OBJC_MAIN_TRANSLATE, type: TranslateTask,
                    dependsOn: TASK_J2OBJC_DEPENDENCY_TRANSLATE_MAIN) {
                group 'j2objc'
                description "Translates main java source files to Objective-C"
                _buildContext = buildContext
            }

            tasks.create(name: TASK_J2OBJC_TEST_TRANSLATE, type: TranslateTask,
                    dependsOn: TASK_J2OBJC_DEPENDENCY_TRANSLATE_TEST) {
                group 'j2objc'
                description "Translates test java source files to Objective-C"
                _buildContext = buildContext

                // Output directories of 'j2objcTranslate', input for all other tasks
                testBuild = true
            }

            afterEvaluate {

                J2objcVersionManager.checkJ2objcConfig(project, false)

//                dependencyResolver.configureAll()

                addManagedPods(
                        tasks,
                        FrameworkConfig.findMain(project),
                        buildContext,
                        false,
                        TASK_J2OBJC_FRAMEWORK_MAIN
                )

                boolean skipTests = J2objcConfig.from(project).skipTests
                if(!skipTests) {
                    addManagedPods(
                            tasks,
                            FrameworkConfig.findTest(project),
                            buildContext,
                            true,
                            TASK_J2OBJC_FRAMEWORK_TEST
                    )
                }

                if(!J2objcConfig.from(project).skipDependsTasks) {
                    buildContext.getBuildTypeProvider().configureDependsOn(project, j2objcPreBuildTask, j2objcContextBuildTask)
                    buildContext.getBuildTypeProvider().configureTestDependsOn(project, j2objcPreBuildTask, j2objcContextBuildTask)
                }
            }

            tasks.create(name: TASK_J2OBJC_ASSEMBLY, type: J2objcAssemblyTask,
                    dependsOn: TASK_J2OBJC_MAIN_TRANSLATE) {
                group 'j2objc'
                description 'Pull together j2objc pieces for library projects'

                _buildContext = buildContext
            }

            tasks.create(name: TASK_J2OBJC_ARCHIVE, type: Jar, dependsOn: TASK_J2OBJC_ASSEMBLY) {
                group 'j2objc'
                description 'Create archive of j2objc dependency'

                from j2objcInfo.rootAssemblyFile()
                extension 'dop'
            }

            tasks.create(name: TASK_TEST_CLASS_LISTING, type: TestTranslateTask,
                    dependsOn: TASK_J2OBJC_TEST_TRANSLATE) {
                group 'j2objc'
                description "Compiles a list of the test classes in your project"
                _buildContext = buildContext

                output = file("${project.buildDir}/$J2objcInfo.TEST_CLASSES_LIST_FILENAME")
            }

            tasks.create(name: TASK_J2OBJC_FRAMEWORK_MAIN, type: FrameworkTask,
                    dependsOn: [TASK_J2OBJC_ASSEMBLY, TASK_J2OBJC_DEPENDENCY_TRANSLATE_MAIN]) {
                group 'j2objc'
                description 'Create framework podspec'
                test = false
                _buildContext = buildContext
            }

            tasks.create(name: TASK_J2OBJC_FRAMEWORK_TEST, type: FrameworkTask,
                    dependsOn: [TASK_TEST_CLASS_LISTING, TASK_J2OBJC_DEPENDENCY_TRANSLATE_TEST]) {
                group 'j2objc'
                description 'Create framework podspec'
                test = true
                _buildContext = buildContext
            }

            tasks.create(name: TASK_J2OBJC_BUILD, type: DefaultTask,
                    dependsOn: [TASK_J2OBJC_FRAMEWORK_MAIN, TASK_J2OBJC_FRAMEWORK_TEST]) {
                group 'j2objc'
                description 'The main task. Run J2objc on Java classes and dependencies.'
            }

            // j2objcCycleFinder must be run manually with ./gradlew j2objcCycleFinder
           tasks.create(name: TASK_J2OBJC_CYCLE_FINDER, type: CycleFinderTask,
                   dependsOn: TASK_J2OBJC_CONTEXT_BUILD) {
               group 'j2objc'
               description "Run the cycle_finder tool on all Java source files"

               _buildContext = buildContext
           }

            tasks.create(name: TASK_J2OBJC_CLEAN_RUNTIME, type: CleanJ2objcRuntimeTask)
        }
    }

    void addManagedPods(TaskContainer tasks, FrameworkConfig frameworkConfig, BuildContext buildContext, boolean test, String upstreamTaskName){
        int count = 0
        for (String managedPod : frameworkConfig.managedPodsList) {
            PodManagerTask.addPodManagerTask(tasks,
                    managedPod,
                    buildContext,
                    test,
                    tasks.getByName(TASK_J2OBJC_BUILD),
                    tasks.getByName(upstreamTaskName),
                    count++
            )
        }
    }
}
