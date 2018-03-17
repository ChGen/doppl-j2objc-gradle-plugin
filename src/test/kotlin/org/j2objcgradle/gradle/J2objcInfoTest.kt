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

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.lang.IllegalArgumentException
import kotlin.test.fail

class J2objcInfoTest {
    @Test
    fun testFolderStructure()
    {
        val j2objcInfo = J2objcInfo.getInstance(File("."))
        checkPath(j2objcInfo.rootBuildFile(), "j2objcBuild")
        checkPath(j2objcInfo.dependencyBuildFile(), "j2objcBuild/dependencies")
        checkPath(j2objcInfo.dependencyExplodedFile(), "j2objcBuild/dependencies/exploded")
        checkPath(j2objcInfo.dependencyExplodedJ2objcFile(), "j2objcBuild/dependencies/exploded/j2objc")
        checkPath(j2objcInfo.dependencyExplodedJ2objcOnlyFile(), "j2objcBuild/dependencies/exploded/j2objcOnly")
        checkPath(j2objcInfo.dependencyExplodedTestJ2objcFile(), "j2objcBuild/dependencies/exploded/testJ2objc")

        checkPath(j2objcInfo.dependencyOutFile(), "j2objcBuild/dependencies/out")
        checkPath(j2objcInfo.dependencyOutFileMain(), "j2objcBuild/dependencies/out/main")
        checkPath(j2objcInfo.dependencyOutFileTest(), "j2objcBuild/dependencies/out/test")
        checkPath(j2objcInfo.dependencyOutMainMappings(), "j2objcBuild/dependencies/out/main/j2objc.mappings")
        checkPath(j2objcInfo.dependencyOutTestMappings(), "j2objcBuild/dependencies/out/test/j2objc.mappings")

        checkPath(j2objcInfo.sourceBuildFile(), "j2objcBuild/source")

        checkPath(j2objcInfo.sourceBuildObjcFile(), "j2objcBuild/source/objc")
        checkPath(j2objcInfo.sourceBuildObjcFileForPhase("main"), "j2objcBuild/source/objc/main")
        checkPath(j2objcInfo.sourceBuildObjcFileMain(), "j2objcBuild/source/objc/main")
        checkPath(j2objcInfo.sourceBuildObjcFileForPhase("test"), "j2objcBuild/source/objc/test")
        checkPath(j2objcInfo.sourceBuildObjcFileTest(), "j2objcBuild/source/objc/test")

        try {
            checkPath(j2objcInfo.sourceBuildObjcFileForPhase("asdf"), "j2objcBuild/source/objc/asdf")
            fail("Shouldn't be allowed")
        } catch (e: IllegalArgumentException) {
        }

        checkPath(j2objcInfo.sourceBuildOutFile(), "j2objcBuild/source/out")
        checkPath(j2objcInfo.sourceBuildOutFileMain(), "j2objcBuild/source/out/main")
        checkPath(j2objcInfo.sourceBuildOutFileTest(), "j2objcBuild/source/out/test")
        checkPath(j2objcInfo.sourceBuildOutMainMappings(), "j2objcBuild/source/out/main/j2objc.mappings")
        checkPath(j2objcInfo.sourceBuildOutTestMappings(), "j2objcBuild/source/out/test/j2objc.mappings")
    }

    fun checkPath(f: File, path: String)
    {
        Assert.assertTrue(f.path.endsWith(path))
    }
}