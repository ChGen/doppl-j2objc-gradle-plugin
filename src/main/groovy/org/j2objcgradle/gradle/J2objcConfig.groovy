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
import org.gradle.api.Project
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil
import org.j2objcgradle.gradle.tasks.Utils

/**
 * j2objcConfig is used to configure the plugin with the project's build.gradle.
 */
@CompileStatic
class J2objcConfig {
    static J2objcConfig from(Project project) {
        return project.extensions.findByType(J2objcConfig)
    }

    final protected Project project

    J2objcConfig(Project project) {
        assert project != null
        this.project = project
        this.podName = project.name
    }

    private J2objcConfig(){}

    String podName

    void podName(String podName) {
        this.podName = podName
    }

    boolean skipTests = false

    boolean emitLineDirectives = false
    boolean dependenciesEmitLineDirectives = false

    void javaDebug(boolean b)
    {
        emitLineDirectives = b
    }

    void dependenciesJavaDebug(boolean b)
    {
        dependenciesEmitLineDirectives = b
    }

    boolean skipDependsTasks = false
    boolean skipDefaultDependencies = false

    String targetVariant = "debug"

    boolean disableAnalytics = false

    /**
     * Additional generated source files directories
     */
    List<String> generatedSourceDirs = new ArrayList<>()
    List<String> generatedTestSourceDirs = new ArrayList<>()

    /**
     * Command line arguments for j2objc cycle_finder.
     * <p/>
     * A list of all possible arguments can be found here:
     * http://j2objc.org/docs/cycle_finder.html
     */
    List<String> cycleFinderArgs = new ArrayList<>()

    /**
     * Command line arguments for j2objc translate.
     * <p/>
     * A list of all possible arguments can be found here:
     * http://j2objc.org/docs/j2objc.html
     */
    List<String> translateArgs = new ArrayList<>()

    Map<String, String> translatedPathPrefix = new HashMap<>()

    /**
     * Additional Java libraries that are part of the j2objc distribution.
     * <p/>
     * For example:
     * <pre>
     * translateJ2objcLibs = ["j2objc_junit.jar", "jre_emul.jar"]
     * </pre>
     */
    // Issue with j2objc process. It needs package level annotations as jars, so
    // we include jsr305 here
    // J2objc default libraries, from $J2OBJC_HOME/lib/...
    // TODO: auto add libraries based on java dependencies, warn on version differences
    List<String> translateJ2objcLibs = ["jre_emul.jar","jsr305-3.0.0.jar"]

    /**
     * Sets the filter on files to translate.
     * <p/>
     * If no pattern is specified, all files within the sourceSets are translated.
     * <p/>
     * This filter is applied on top of all files within the 'main' and 'test'
     * java sourceSets.  Use {@link #translatePattern(groovy.lang.Closure)} to
     * configure.
     */
    PatternSet translatePattern = null

    PatternSet testIdentifier = null

    /**
     * Add generated source files directories, e.g. from dagger annotations.
     *
     * @param generatedSourceDirs adds generated source directories for j2objc translate
     */
    void generatedSourceDirs(String... generatedSourceDirs) {
        Utils.appendArgs(this.generatedSourceDirs, 'generatedSourceDirs', true, generatedSourceDirs)
    }

    /**
     * Add generated source files directories, e.g. from dagger annotations.
     *
     * @param generatedSourceDirs adds generated source directories for j2objc translate
     */
    void generatedTestSourceDirs(String... generatedTestSourceDirs) {
        Utils.appendArgs(this.generatedTestSourceDirs, 'generatedTestSourceDirs', true, generatedTestSourceDirs)
    }

    /**
     * Add command line arguments for j2objc cycle_finder.
     * <p/>
     * A list of all possible arguments can be found here:
     * http://j2objc.org/docs/cycle_finder.html
     *
     * @param cycleFinderArgs add args for 'cycle_finder' tool
     */
    void cycleFinderArgs(String... cycleFinderArgs) {
        Utils.appendArgs(this.cycleFinderArgs, 'cycleFinderArgs', true, cycleFinderArgs)
    }

    /**
     * Add command line arguments for j2objc translate.
     * <p/>
     * A list of all possible arguments can be found here:
     * http://j2objc.org/docs/j2objc.html
     *
     * @param translateArgs add args for the 'j2objc' tool
     */
    void translateArgs(String... translateArgs) {
        Utils.appendArgs(this.translateArgs, 'translateArgs', true, translateArgs)
    }

    List<String> processedTranslateArgs()
    {
        return translateArgs
    }

    void translatedPathPrefix(String path, String prefix)
    {
        translatedPathPrefix.put(path, prefix)
    }

    /**
     * Configures the {@link #translatePattern}.
     * <p/>
     * Calling this method repeatedly further modifies the existing translatePattern,
     * and will create an empty translatePattern if none exists.
     * <p/>
     * For example:
     * <pre>
     * translatePattern {
     *     exclude 'CannotTranslateFile.java'
     *     exclude '**&#47;CannotTranslateDir&#47;*.java'
     *     include '**&#47;CannotTranslateDir&#47;AnExceptionToInclude.java'
     * }
     * </pre>
     * @see
     * <a href="https://docs.gradle.org/current/userguide/working_with_files.html#sec:file_trees">Gradle User Guide</a>
     */
    PatternSet translatePattern(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PatternSet) Closure cl) {
        if (translatePattern == null) {
            translatePattern = new PatternSet()
        }
        return ConfigureUtil.configure(cl, translatePattern)
    }

    PatternSet testIdentifier(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PatternSet) Closure cl) {
        if (testIdentifier == null) {
            testIdentifier = new PatternSet()
        }
        return ConfigureUtil.configure(cl, testIdentifier)
    }
}
