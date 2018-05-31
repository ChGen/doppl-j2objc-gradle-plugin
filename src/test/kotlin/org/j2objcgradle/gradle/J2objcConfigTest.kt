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

import org.j2objcgradle.gradle.utils.replaceFile
import org.j2objcgradle.gradle.utils.validateFileContent
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.Matchers
import org.hamcrest.Matchers.isOneOf
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Matchers.eq
import java.io.File
import kotlin.test.assertEquals


class J2objcConfigTest : BasicTestBase() {

    @Test
    fun testJavaDebug()
    {
        writeRunCustomConfig(config = "javaDebug true")
        assertTrue("Line directive not found", validateFileContent(File(projectFolder, "build/j2objcBuild/translated/main/mainSourceOut.m"), { s ->
            return@validateFileContent s.lines().any { it.startsWith("#line") && it.endsWith("src/main/java/co/touchlab/basicjava/GoBasicJava.java\"") }
        }))

        writeRunCustomConfig(config = "//javaDebug true")
        assertTrue("Line directives still there", validateFileContent(File(projectFolder, "build/j2objcBuild/translated/main/mainSourceOut.m"), { s ->
            return@validateFileContent !s.contains("#line")
        }))
    }

    @Test
    fun testDependenciesJavaDebug()
    {
        writeRunCustomConfig(config = "dependenciesJavaDebug true", depends =
        """
            compile "com.google.code.gson:gson:2.6.2"
            doppl "co.doppl.com.google.code.gson:gson:2.6.2.7"
        """)

        val objcPath = "build/j2objcBuild/translated/main/mainDependenciesOut.m"
        assertTrue("Line directive not found", validateFileContent(File(projectFolder, objcPath), { s ->
            return@validateFileContent s.lines().any { it.startsWith("#line") && it.endsWith(".java\"") }
        }))

        writeRunCustomConfig(config = "//dependenciesJavaDebug true", depends =
        """
            compile "com.google.code.gson:gson:2.6.2"
            doppl "co.doppl.com.google.code.gson:gson:2.6.2.7"
        """)
        assertTrue("Line directives still there", validateFileContent(File(projectFolder, objcPath), { s ->
            return@validateFileContent s.lines().none { it.startsWith("#line") }
        }))
    }

    @Test
    fun testTranslatedPathPrefix()
    {

        writeRunCustomConfig(config = "translatedPathPrefix 'co.touchlab.basicjava', 'TBJ'")
        assertTrue("Prefix incorrectly generated", validateFileContent(File(projectFolder, "build/prefixes.properties"), { s ->
            return@validateFileContent s.contains("co.touchlab.basicjava=TBJ")
        }))

        val rerunResult = buildResult()
        assertEquals(rerunResult.task(":j2objcMainTranslate").outcome, TaskOutcome.UP_TO_DATE)

        writeRunCustomConfig(config = "translatedPathPrefix 'co.touchlab.basicjava', 'GBJ'")
        assertTrue("Prefix incorrectly generated", validateFileContent(File(projectFolder, "build/prefixes.properties"), { s ->
            return@validateFileContent s.contains("co.touchlab.basicjava=GBJ")
        }))
    }

    fun testTranslatedPathPrefixDependencies()
    {
        writeRunCustomConfig(config = "translatedPathPrefix 'co.touchlab.basicjava', 'TBJ'")
        assertTrue("Prefix incorrectly generated", validateFileContent(File(projectFolder, "build/prefixes.properties"), { s ->
            return@validateFileContent s.contains("co.touchlab.basicjava=TBJ")
        }))

        val rerunResult = buildResult()
        assertEquals(rerunResult.task(":j2objcMainTranslate").outcome, TaskOutcome.UP_TO_DATE)

        writeRunCustomConfig(config = "translatedPathPrefix 'co.touchlab.basicjava', 'GBJ'")
        assertTrue("Prefix incorrectly generated", validateFileContent(File(projectFolder, "build/prefixes.properties"), { s ->
            return@validateFileContent s.contains("co.touchlab.basicjava=GBJ")
        }))
    }

    @Test
    fun testIdentifier()
    {
        writeRunCustomConfig()
        assertTrue("Test classes not found in ${J2objcInfo.TEST_CLASSES_LIST_FILENAME}", validateFileContent(File(projectFolder, "build/${J2objcInfo.TEST_CLASSES_LIST_FILENAME}"), { s ->
            return@validateFileContent s.contains("co.touchlab.basicjava.AnotherBasicTest") && s.contains("co.touchlab.basicjava.BasicJavaTest")
        }))

        val rerunResult = buildResult()
        assertEquals(rerunResult.task(":j2objcTestTranslate").outcome, TaskOutcome.UP_TO_DATE)

        writeRunCustomConfig(config = """
            testIdentifier {
                include 'co/touchlab/basicjava/BasicJavaTest.java'
            }
        """)
        assertTrue("Incorrect number of test classes found in ${J2objcInfo.TEST_CLASSES_LIST_FILENAME}", validateFileContent(File(projectFolder, "build/${J2objcInfo.TEST_CLASSES_LIST_FILENAME}"), { s ->
            return@validateFileContent s.contains("co.touchlab.basicjava.BasicJavaTest")
        }))
    }

    @Test
    fun testManagePod()
    {
        writeRunCustomConfig(config = """
    podName "custompodname"
    mainFramework{
        managePod "ios"
    }
        """)

    }


    fun writeRunCustomConfig(depends: String = "", config: String = "")
    {
        writeCustomConfig(depends = depends, config = config)

        val result = buildResult()

        assertThat(result.task(":${J2objcPlugin.TASK_J2OBJC_BUILD}").outcome, isOneOf(TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE))
    }

    private fun buildResult(): BuildResult {
        return GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(projectFolder)
                .forwardOutput()
                .withArguments("${J2objcPlugin.TASK_J2OBJC_BUILD}")
                .build()
    }

    fun writeCustomConfig(depends: String = "", config: String = "")
    {
        replaceFile(projectFolder, "build.gradle", """
    plugins {
        id 'java'
        id 'org.j2objcgradle.gradle'
    }

    group 'co.touchlab'
    version '1.2.3'

    sourceCompatibility = 1.8

    repositories {
        mavenCentral()
        maven { url 'https://dl.bintray.com/doppllib/maven2' }
    }

    dependencies {
        $depends
    }

    j2objcConfig {
        $config
    }

                """)
    }
}