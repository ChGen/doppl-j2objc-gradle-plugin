/*
 * Copyright (c) 2015 the authors of j2objc-gradle (see AUTHORS file)
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

package com.github.j2objccontrib.j2objcgradle.tasks

import com.github.j2objccontrib.j2objcgradle.J2objcConfig
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.incremental.InputFileDetails
import org.junit.Before
import org.junit.Test

/**
 * TranslateTask tests.
 */
class TranslateTaskTest {

    private Project proj
    private String j2objcHome
    private J2objcConfig j2objcConfig

    @Before
    void setUp() {
        // Default to native OS except for specific tests
        Utils.setFakeOSNone()
        (proj, j2objcHome, j2objcConfig) = TestingUtils.setupProject(
                new TestingUtils.ProjectConfig(applyJavaPlugin: true, createJ2objcConfig: true))
    }

    // TODO: add java source files to the test cases
    // TODO: perhaps even better, point the project towards an existing example

    @Test
    void translate_BasicArguments() {
        TranslateTask j2objcTranslate = (TranslateTask) proj.tasks.create(
                name: 'j2objcTranslate', type: TranslateTask) {
            srcGenDir = proj.file(proj.file('build/j2objcSrcGen').absolutePath)
        }

        MockProjectExec mockProjectExec = new MockProjectExec(proj, j2objcHome)
        mockProjectExec.demandExecAndReturn([
                '/J2OBJC_HOME/j2objc',
                '-d', '/PROJECT_DIR/build/j2objcSrcGen',
                '-sourcepath', '/PROJECT_DIR/src/main/java:/PROJECT_DIR/src/test/java',
                '-classpath', '/J2OBJC_HOME/lib/j2objc_annotations.jar:/J2OBJC_HOME/lib/j2objc_guava.jar:/J2OBJC_HOME/lib/j2objc_junit.jar:/J2OBJC_HOME/lib/jre_emul.jar:/J2OBJC_HOME/lib/javax.inject-1.jar:/J2OBJC_HOME/lib/jsr305-3.0.0.jar:/J2OBJC_HOME/lib/mockito-core-1.9.5.jar:/PROJECT_DIR/build/classes'
        ],
        // expectedWindowsExecutableAndArgs
        [
                'java',
                '-jar',
                '/J2OBJC_HOME/lib/j2objc.jar'
        ])

        j2objcTranslate.translate(genNonIncrementalInputs())

        mockProjectExec.verify()
    }

    @Test
    void translate_Windows() {
        Utils.setFakeOSWindows()
        TranslateTask j2objcTranslate = (TranslateTask) proj.tasks.create(
                name: 'j2objcTranslate', type: TranslateTask) {
            srcGenDir = proj.file(proj.file('build/j2objcSrcGen').absolutePath)
        }

        MockProjectExec mockProjectExec = new MockProjectExec(proj, j2objcHome)
        mockProjectExec.demandExecAndReturn([
                'INVALID-NEEDS-WINDOWS-SUBSTITUTION',
                '-d', '/PROJECT_DIR/build/j2objcSrcGen',
                '-sourcepath', '/PROJECT_DIR/src/main/java;/PROJECT_DIR/src/test/java',
                '-classpath', '/J2OBJC_HOME/lib/j2objc_annotations.jar;/J2OBJC_HOME/lib/j2objc_guava.jar;/J2OBJC_HOME/lib/j2objc_junit.jar;/J2OBJC_HOME/lib/jre_emul.jar;/J2OBJC_HOME/lib/javax.inject-1.jar;/J2OBJC_HOME/lib/jsr305-3.0.0.jar;/J2OBJC_HOME/lib/mockito-core-1.9.5.jar;/PROJECT_DIR/build/classes'
        ],
        // expectedWindowsExecutableAndArgs
        [
                'java',
                 '-jar',
                 '/J2OBJC_HOME/lib/j2objc.jar',
        ])

        j2objcTranslate.translate(genNonIncrementalInputs())

        mockProjectExec.verify()
    }

    @Test
    void translate_J2objcConfig() {
        TranslateTask j2objcTranslate = (TranslateTask) proj.tasks.create(
                name: 'j2objcTranslate', type: TranslateTask) {
            srcGenDir = proj.file('build/j2objcSrcGen')
        }
        // Tests multiple values with absolute and relative paths
        String absGenPath = TestingUtils.windowsNoFakeAbsolutePath('/ABS-GENPATH')
        String absSourcePath = TestingUtils.windowsNoFakeAbsolutePath('/ABS-SOURCEPATH')
        String absClassPath = TestingUtils.windowsNoFakeAbsolutePath('/ABS-CLASSPATH')
        j2objcConfig.generatedSourceDirs('REL-GENPATH', absGenPath)
        j2objcConfig.translateSourcepaths('REL-SOURCEPATH', absSourcePath)
        j2objcConfig.translateClasspaths('REL-CLASSPATH', absClassPath)
        j2objcConfig.translateJ2objcLibs = ['J2OBJC-LIB1', 'J2OBJC-LIB2']
        j2objcConfig.translateArgs('-ARG1', '-ARG2')
        // TODO: add testing for translatePattern
        // j2objcConfig.translatePattern {
        //     exclude '**/Example.java'
        // }

        MockProjectExec mockProjectExec = new MockProjectExec(proj, j2objcHome)
        mockProjectExec.demandExecAndReturn([
                '/J2OBJC_HOME/j2objc',
                '-d', '/PROJECT_DIR/build/j2objcSrcGen',
                '-sourcepath', "/PROJECT_DIR/src/main/java:/PROJECT_DIR/src/test/java:/PROJECT_DIR/REL-SOURCEPATH:$absSourcePath:/PROJECT_DIR/REL-GENPATH:$absGenPath",
                '-classpath', "/PROJECT_DIR/REL-CLASSPATH:$absClassPath:/J2OBJC_HOME/lib/J2OBJC-LIB1:/J2OBJC_HOME/lib/J2OBJC-LIB2:/PROJECT_DIR/build/classes",
                '-ARG1', '-ARG2',
        ],
        // expectedWindowsExecutableAndArgs
        [
                'java',
                '-jar',
                '/J2OBJC_HOME/lib/j2objc.jar'
        ])

        j2objcTranslate.translate(genNonIncrementalInputs())

        mockProjectExec.verify()
    }


    // Utility Method
    private static IncrementalTaskInputs genNonIncrementalInputs() {
        IncrementalTaskInputs incrementalTaskInputs = new IncrementalTaskInputs() {
            @Override
            boolean isIncremental() { return false }

            @Override
            void outOfDate(Action<? super InputFileDetails> outOfDateAction) {}

            @Override
            void removed(Action<? super InputFileDetails> removedAction) {}
        }
        return incrementalTaskInputs
    }
}
