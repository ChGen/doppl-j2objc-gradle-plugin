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


import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.io.FileMatchers
import org.j2objcgradle.gradle.utils.findObjcClassDefinition
import org.j2objcgradle.gradle.utils.replaceFile
import org.jrubyparser.CompatVersion
import org.jrubyparser.NodeVisitor
import org.jrubyparser.Parser
import org.jrubyparser.ast.*
import org.jrubyparser.parser.ParserConfiguration
import org.jrubyparser.util.NoopVisitor
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.Matchers
import java.io.File
import java.io.StringReader
import java.util.*
import java.util.Arrays.asList
import kotlin.collections.ArrayList
import kotlin.test.assertTrue

class BasicJ2objcProjectTest : BasicTestBase() {

    @Test
    fun j2objcBuildWritesClassTest()
    {
        runProjectBuild()

        val mainBuildDir = File(projectFolder, "build/j2objcBuild/translated/main")
        val headerFile = File(mainBuildDir, "mainSourceOut.h")

        findObjcClassDefinition(headerFile, "CoTouchlabBasicjavaGoBasicJava")
    }

    @Test
    fun checkDependenciesAreExtracted()
    {
        runGsonDependencyBuild()

        assertThat(File(projectFolder, "build/j2objcBuild/dependencies/exploded/main/co_doppl_com_google_code_gson_gson_2_6_2_7"), FileMatchers.anExistingDirectory())
        assertThat(File(projectFolder, "build/j2objcBuild/dependencies/exploded/main/co_doppl_com_google_code_gson_gson_2_6_2_7/java/com/google/gson/Gson.java"), FileMatchers.anExistingFile())

        val headerFile = File(projectFolder, "build/j2objcBuild/translated/main/mainDependenciesOut.h")
        findObjcClassDefinition(headerFile, "ComGoogleGsonGson")
    }

    @Test
    fun checkPodspec() {

        runGsonDependencyBuild()

        val spec = File(projectFolder, "basicjava.podspec")
        assertThat(spec, FileMatchers.anExistingFile())
        assertEquals(spec.find("name="), "basicjava")
        assertEquals(spec.find("homepage="), "homepage")
        assertEquals(spec.findMap("authors=").get("Filler"), "filler@example.com")
        assertEquals(spec.findMap("source=").get("git"), "https://random.com")
        assertEquals(spec.find("source_files="), "src/**/*.{h,m,cpp}")
        assertEquals(asList("src/mainSourceOut.h", "src/mainDependenciesOut.h"), spec.findList("public_header_files="))
        assertEquals(spec.find("header_mappings_dir="), "src")
        assertEquals(asList("z", "sqlite3", "iconv", "jre_emul"), spec.findList("libraries="))
        val podTargetXCConfig = spec.findMap("pod_target_xcconfig=")
        assertEquals("\$(J2OBJC_LOCAL_PATH)/include", podTargetXCConfig.get("HEADER_SEARCH_PATHS"))
        assertEquals("\$(J2OBJC_LOCAL_PATH)/lib", podTargetXCConfig.get("LIBRARY_SEARCH_PATHS"))
        assertEquals("-ObjC", podTargetXCConfig.get("OTHER_LDFLAGS"))
    }

    @Test
    fun checkTestPodspec() {

        runGsonDependencyBuild()

        val spec = File(projectFolder, "basicjavaTest.podspec")
        assertThat(spec, FileMatchers.anExistingFile())
        assertEquals(spec.find("name="), "basicjavaTest")
        assertEquals(spec.find("homepage="), "homepageOfTest")
        assertEquals(spec.findMap("authors=").get("Filler"), "testfiller@example.com")
        assertEquals(spec.findMap("source=").get("git"), "https://testrandom.com")
        assertEquals(spec.find("source_files="), "src/**/*.{h,m,cpp}")
        assertEquals(asList("src/testSourceOut.h", "src/testDependenciesOut.h"), spec.findList("public_header_files="))
        assertEquals(spec.find("header_mappings_dir="), "src")
        assertEquals(asList("z", "sqlite3", "iconv", "jre_emul"), spec.findList("libraries="))
        val podTargetXCConfig = spec.findMap("pod_target_xcconfig=")
        assertEquals("\$(J2OBJC_LOCAL_PATH)/include", podTargetXCConfig.get("HEADER_SEARCH_PATHS"))
        assertEquals("\$(J2OBJC_LOCAL_PATH)/lib", podTargetXCConfig.get("LIBRARY_SEARCH_PATHS"))
        assertEquals("-ObjC", podTargetXCConfig.get("OTHER_LDFLAGS"))
    }

    @Test
    fun checkMainFrameworkPod() {
        val result = runOkioDependencyBuild()

        assertEquals(result.task(":assembleMainFrameworkPod").getOutcome(), SUCCESS)

        val mainPodDir = "$projectFolder/build/pods/main"
        assertThat(File(mainPodDir), FileMatchers.anExistingDirectory())
        listOf("$mainPodDir/src/mainSourceOut.h",
                "$mainPodDir/src/mainSourceOut.m")
                .map { File(it) }
                .forEach { assertThat("Translated sources are not present in pod", it, FileMatchers.anExistingFile()) }
        listOf( "$mainPodDir/src/mainDependenciesOut.h",
                "$mainPodDir/src/mainDependenciesOut.m")
                .map { File(it) }
                .forEach { assertThat("Translated dependencies are not present in pod", it, FileMatchers.anExistingFile()) }
        assertThat("Dependency sources are not present in pod",
                File("$mainPodDir/src/com/google/j2objc/LibraryNotLinkedError.h"),
                FileMatchers.anExistingFile())
        assertThat("Podspec is not present in pod",
            File("$mainPodDir/basicjava.podspec"),
            FileMatchers.anExistingFile())
    }

    @Test
    fun checkArchiveMainFrameworkPod() {
        val result = runOkioDependencyBuild()

        assertEquals(result.task(":archiveMainFrameworkPod").getOutcome(), SUCCESS)

        val mainPod = "$projectFolder/build/distributions/basicjava-1.2.3.tar.gz"
        assertThat(File(mainPod), FileMatchers.anExistingFile())
    }

    private fun File.parse(): Node {
        val rubyParser = Parser()
        val `in` = StringReader(this.readText())
        val version = CompatVersion.RUBY1_8
        val config = ParserConfiguration(0, version)
        val n: Node = rubyParser.parse("<code>", `in`, config)
        return n
    }

    private fun File.find(fieldName: String):String {
        var parsedName = ""
        this.parse().accept(object : NoopVisitor(), NodeVisitor {
            override fun visitAttrAssignNode(iVisited: AttrAssignNode?): Any {
                if (iVisited!!.name == fieldName) {

                    val child = (iVisited.childNodes().get(1) as ListNode).get(0);
                    if (child is StrNode) {
                        parsedName = child.value
                    }
                }
                return Any()
            }
        })
        return parsedName
    }

    private fun File.findMap(fieldName: String):Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        this.parse().accept(object : NoopVisitor(), NodeVisitor {
            override fun visitAttrAssignNode(iVisited: AttrAssignNode?): Any {
                if (iVisited!!.name == fieldName) {
                    val child = (iVisited.childNodes().get(1) as ListNode).get(0);
                    if (child is HashNode) {

                        var lastKey: Node? = null
                        for (i in 0..child.listNode.size()-1) {
                            val isKey = ((i % 2) == 0)
                            val childNode = child.listNode.get(i)
                            if (isKey) {
                                lastKey = childNode
                            } else {
                                var keyStr = ""
                                if (lastKey is StrNode) {
                                    keyStr = lastKey.value
                                } else if (lastKey is SymbolNode) {
                                    keyStr = lastKey.name
                                }

                                map.put(keyStr, (childNode as StrNode).value)
                            }
                        }
                    }
                }
                return Any()
            }
        })
        return map
    }

    private fun File.findList(fieldName: String):List<String> {
        val list: MutableList<String> = ArrayList()
        val f = this
        this.parse().accept(object : NoopVisitor(), NodeVisitor {
            override fun visitAttrAssignNode(iVisited: AttrAssignNode?): Any {
                if (iVisited!!.name == fieldName) {
                    val parsedList = iVisited.childNodes().last().childNodes().first().childNodes().first().childNodes()
                    if (parsedList is ArrayList) {

                        for (childNode in parsedList) {
                            if (childNode is StrNode) {
                                list.add(childNode.value)
                            } else throw AssertionError("List $fieldName children are not valid. Statement: "+f.readLines().get(childNode.position.startLine).trim())
                        }
                    } else throw AssertionError("List $fieldName cannot be parsed. Statement: "+f.readLines().get(iVisited.position.startLine).trim())
                }
                return Any()
            }
        })
        return list
    }

    @Test
    fun checkCycleFinder() {
        createGsonDepBuildFile()

        val result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(projectFolder)
                .withArguments(J2objcPlugin.TASK_J2OBJC_CYCLE_FINDER)
                .build()

        assertEquals(SUCCESS, result.task(":${J2objcPlugin.TASK_J2OBJC_CYCLE_FINDER}").outcome)
    }

    @Test
    fun checkNativeMainCompiles() {
        createGsonDepBuildFile()

        val result = GradleRunner.create()
                .withPluginClasspath()
                .forwardOutput()
                .withProjectDir(projectFolder)
                .withArguments("j2objcDebugStaticLibrary")
                .build()

        assertEquals(SUCCESS, result.task(":j2objcDebugStaticLibrary").outcome)
        assertThat(File("${projectFolder.path}/build/libs/j2objc/static/debug/libj2objc.a"), FileMatchers.anExistingFile())
    }


    @Test
    fun checkNativeTestCanBeBuilt() {
        createGsonDepBuildFile()

        val result = GradleRunner.create()
                .withPluginClasspath()
                .forwardOutput()
                .withProjectDir(projectFolder)
                .withArguments("testJ2objcDebugExecutable")
                .build()

        assertEquals(SUCCESS, result.task(":testJ2objcDebugExecutable").outcome)
        assertThat(File("${projectFolder.path}/build/exe/testJ2objc/debug/testJ2objc"), FileMatchers.anExistingFile())
    }

    @Test
    fun checkNativeTestExecuted() {
        createGsonDepBuildFile()

        val result = GradleRunner.create()
                .withPluginClasspath()
                .forwardOutput()
                .withProjectDir(projectFolder)
                .withArguments("testTranslatedDebug")
                .build()

        assertEquals(SUCCESS, result.task(":testTranslatedDebug").outcome)
    }


    @Test
    fun dependenciesOnlyBuildOnce()
    {
        runGsonDependencyBuild()
        val rerunResult = runProjectBuild()
        assertEquals(UP_TO_DATE, rerunResult.task(":${J2objcPlugin.TASK_J2OBJC_DEPENDENCY_TRANSLATE_MAIN}").outcome)
    }

    private fun runGsonDependencyBuild(): BuildResult {
        createGsonDepBuildFile()

        return runValidateJ2objcBuild()
    }

    private fun createGsonDepBuildFile() {
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
            maven { url 'https://dl.bintray.com/doppllib/j2objc' }
        }

        j2objcConfig {
            mainFramework {
                homepage = "homepage"
                license = "{ :type => 'Private' }"
                authors = "{ 'Filler' => 'filler@example.com' }"
                source = "{ :git => 'https://random.com'}"
                writeActualJ2objcPath = false
            }
            testFramework {
                homepage = "homepageOfTest"
                license = "{ :type => 'TestPrivate' }"
                authors = "{ 'Filler' => 'testfiller@example.com' }"
                source = "{ :git => 'https://testrandom.com'}"
                writeActualJ2objcPath = false
            }
        }

        dependencies {

            compile "com.google.code.gson:gson:2.6.2"
            doppl "co.doppl.com.google.code.gson:gson:2.6.2.7"

            testCompile group: 'junit', name: 'junit', version: '4.12'
            testJ2objc "org.j2objcgradle.junit:junit:4.12.0"
        }

                    """)
    }

    private fun runOkioDependencyBuild(): BuildResult {
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
        maven { url 'https://dl.bintray.com/doppllib/j2objc' }
    }

    j2objcConfig {
        mainFramework {
            homepage = "homepage"
            license = "{ :type => 'Private' }"
            authors = "{ 'Filler' => 'filler@example.com' }"
            source = "{ :git => 'https://random.com'}"
            writeActualJ2objcPath = false
        }
        testFramework {
            homepage = "homepageOfTest"
            license = "{ :type => 'TestPrivate' }"
            authors = "{ 'Filler' => 'testfiller@example.com' }"
            source = "{ :git => 'https://testrandom.com'}"
            writeActualJ2objcPath = false
        }
    }

    dependencies {
        doppl "co.doppl.com.squareup.okio:okio:1.13.0.0"

        compile "com.google.code.gson:gson:2.6.2"
        doppl "co.doppl.com.google.code.gson:gson:2.6.2.7"

        testCompile group: 'junit', name: 'junit', version: '4.12'
        testJ2objc "org.j2objcgradle.junit:junit:4.12.0"
    }

                """)

        return runValidateJ2objcBuild()
    }

    private fun runValidateJ2objcBuild() :BuildResult{
        val result = runProjectBuild()

        assertEquals(result.task(":${J2objcPlugin.TASK_J2OBJC_BUILD}").getOutcome(), SUCCESS)

        return result
    }

    private fun runProjectBuild(): BuildResult {
        return GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(projectFolder)
                .forwardOutput()
                .withArguments(J2objcPlugin.TASK_J2OBJC_BUILD, "--stacktrace")
                .build()
    }
}