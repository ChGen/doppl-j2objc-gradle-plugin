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
import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class AdvancedAndroidProjectTest {

    @Rule
    @JvmField
    var testProjectDir = TemporaryFolder()

    lateinit var projectFolder: File

    private val MODULE = "MyModule"

    @Before
    fun setup()
    {
        projectFolder = testProjectDir.newFolder()
        FileUtils.copyDirectory(File("testprojects/advancedandroid"), projectFolder)
    }

    @Test
    fun translatedPathPrefix()
    {
        writeRunCustomConfig(config = """
            translatedPathPrefix 'co.touchlab.mymodule', 'MM'
            """)

        Assert.assertTrue("Prefix incorrectly generated", validateFileContent(File(projectFolder, "mymodule/build/prefixes.properties"), { s ->
            return@validateFileContent s.contains("co.touchlab.mymodule=MM")
        }))

        val rerunResult = buildResult()
        Assert.assertEquals(rerunResult.task(":$MODULE:j2objcMainTranslate").outcome, TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun testIdentifier()
    {
        writeRunCustomConfig()
        Assert.assertTrue("Test classes not found in ${J2objcInfo.TEST_CLASSES_LIST_FILENAME}", validateFileContent(File(projectFolder, "mymodule/build/${J2objcInfo.TEST_CLASSES_LIST_FILENAME}"), { s ->
            return@validateFileContent s.contains("co.touchlab.mymodule.ModuleTest")
        }))

        val rerunResult = buildResult()
        Assert.assertEquals(rerunResult.task(":$MODULE:j2objcTestTranslate").outcome, TaskOutcome.UP_TO_DATE)
    }

    private fun writeRunCustomConfig(depends: String = "",
                                     config: String = "")
    {
        writeBuildFile(depends, config)
        val result = buildResult()
        for (task in result.tasks) {
            println(task)
        }
        Assert.assertEquals(result.task(":$MODULE:${J2objcPlugin.TASK_J2OBJC_BUILD}").outcome, TaskOutcome.SUCCESS)
    }

    private fun writeBuildFile(depends: String = "",
                               config: String = "")
    {
        replaceFile(projectFolder, "MyModule/build.gradle", """
    plugins {
        id 'java'
        id 'org.j2objcgradle.gradle'
    }

    group 'co.touchlab'
    version '1.2.3'

    sourceCompatibility = 1.8

    repositories {
        maven { url 'https://dl.bintray.com/doppllib/maven2' }
        mavenCentral()
    }

    dependencies {
        testCompile group: 'junit', name: 'junit', version: '4.12'
        $depends
    }

    j2objcConfig {
        $config
    }
        """)
    }

    private fun buildResult(): BuildResult
    {
        return GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(projectFolder)
                .withArguments(J2objcPlugin.TASK_J2OBJC_BUILD)
                .build()
    }
}