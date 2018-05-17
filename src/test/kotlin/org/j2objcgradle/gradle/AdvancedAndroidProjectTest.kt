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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.StringReader
import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.*;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.util.NoopVisitor
import org.junit.Assert.*
import java.io.BufferedReader
import java.io.FileReader


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

        assertTrue("Prefix incorrectly generated", validateFileContent(File(projectFolder, "mymodule/build/prefixes.properties"), { s ->
            return@validateFileContent s.contains("co.touchlab.mymodule=MM")
        }))

        val rerunResult = buildResult()
        assertEquals(rerunResult.task(":$MODULE:j2objcMainTranslate").outcome, TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun testIdentifier()
    {
        writeRunCustomConfig()
        assertTrue("Test classes not found in ${J2objcInfo.TEST_CLASSES_LIST_FILENAME}", validateFileContent(File(projectFolder, "mymodule/build/${J2objcInfo.TEST_CLASSES_LIST_FILENAME}"), { s ->
            return@validateFileContent s.contains("co.touchlab.mymodule.ModuleTest")
        }))

        val rerunResult = buildResult()
        assertEquals(rerunResult.task(":$MODULE:j2objcTestTranslate").outcome, TaskOutcome.UP_TO_DATE)
    }


    @Test
    fun headerIncludesAreRelative()
    {
        val deps = """
                compile "io.reactivex.rxjava2:rxjava:2.1.5"
                j2objc "co.doppl.io.reactivex.rxjava2:rxjava:2.1.5.4"

                compile "io.reactivex.rxjava2:rxandroid:2.0.1"
                j2objc "co.doppl.io.reactivex.rxjava2:rxandroid:2.0.1.7"

            """
        val config = """
            podName "j2objclib"
            mainFramework {
                writeActualJ2objcPath = false
            }

            """
        writeBuildFile(config = config, depends = deps)

        val result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(projectFolder)
                .withArguments(J2objcPlugin.TASK_J2OBJC_FRAMEWORK_MAIN)
                .build()

        assertEquals(result.task(":$MODULE:${J2objcPlugin.TASK_J2OBJC_FRAMEWORK_MAIN}").outcome, TaskOutcome.SUCCESS)
        assertHeaderSearchPathsAreRelativeInPodspec()
    }

    private fun assertHeaderSearchPathsAreRelativeInPodspec() {
        val targetXcodeConfig: MutableMap<String, String> = parseTargetXcodeConfigMap(readPodspecFile())
        var anyPathBeginsWithSlash = false
        if (targetXcodeConfig.containsKey("HEADER_SEARCH_PATHS")) {
            val searchPath: String  = targetXcodeConfig.get("HEADER_SEARCH_PATHS")!!
            val searchPaths = searchPath.split(" ")
            anyPathBeginsWithSlash = searchPaths.none { it.startsWith("/") }
            assertTrue("Header includes contain absolute paths: "+searchPaths, anyPathBeginsWithSlash)
        } else {
            fail("HEADER_SEARCH_PATHS not found in podspec")
        }
    }

    private fun readPodspecFile(): String {
        val reader = BufferedReader(FileReader(File(projectFolder, "/mymodule/j2objclib.podspec")))
        var readText = ""
        reader.use { reader ->
            readText = reader.readText()
        }
        return readText
    }

    private fun parseTargetXcodeConfigMap(s: String): MutableMap<String, String> {
        val rubyParser = Parser()
        val `in` = StringReader(s)
        val version = CompatVersion.RUBY1_8
        val config = ParserConfiguration(0, version)
        val n: Node = rubyParser.parse("<code>", `in`, config)
        var map: MutableMap<String, String> = HashMap()

        val POD_TARGET_XCCONFIG_NAME = "pod_target_xcconfig="

        n.accept(object : NoopVisitor() {
            override fun visitAttrAssignNode(iVisited: AttrAssignNode?): Any {
                if (iVisited?.name == "pod_target_xcconfig=") {
                    iVisited.childNodes().forEach { it.accept(this) }
                }
                return Any()
            }

            override fun visitListNode(iVisited: ListNode?): Any {

                if (iVisited?.parent is AttrAssignNode && (iVisited?.parent as AttrAssignNode).name == POD_TARGET_XCCONFIG_NAME) {
                    iVisited?.childNodes().forEach { it.accept(this) }
                }
                return Any()
            }

            override fun visitHashNode(iVisited: HashNode?): Any {
                if (iVisited?.grandParent is AttrAssignNode && (iVisited?.grandParent as AttrAssignNode).name == POD_TARGET_XCCONFIG_NAME) {
                    iVisited.childNodes().forEach { it.accept(this) }
                }
                return Any()
            }

            override fun visitArrayNode(iVisited: ArrayNode?): Any {
                if (iVisited?.parent?.grandParent is AttrAssignNode && (iVisited?.parent?.grandParent as AttrAssignNode).name == POD_TARGET_XCCONFIG_NAME) {

                    var key: Boolean = true
                    var lastKey: StrNode? = null
                    for (childNode in iVisited.childNodes()) {
                        if (key) {
                            lastKey = childNode as StrNode
                        } else {
                            map.put(lastKey?.value!!, (childNode as StrNode).value)
                        }
                        key = !key;
                    }

                }
                return Any()
            }
        })
        return map
    }


    private fun writeRunCustomConfig(depends: String = "",
                                     config: String = "")
    {
        writeBuildFile(depends, config)
        val result = buildResult()
        for (task in result.tasks) {
            println(task)
        }
        assertEquals(result.task(":$MODULE:${J2objcPlugin.TASK_J2OBJC_BUILD}").outcome, TaskOutcome.SUCCESS)
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