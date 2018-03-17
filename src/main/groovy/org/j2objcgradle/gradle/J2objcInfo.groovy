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

import com.google.common.annotations.VisibleForTesting
import org.gradle.api.Project

class J2objcInfo {
    public static final String MAIN = "main"
    public static final String TEST = "test"

    //Stable jar java dirs
    public static final String JAVA_SOURCE = "javasource"

    public static final String J2OBJC_BUILD = "j2objcBuild"
    public static final String J2OBJC_ASSEMBLY = "j2objcAssembly"
    public static final String DEPENDENCIES = "dependencies"
    public static final String JAR = "jar"
    public static final String SOURCE = "source"
    public static final String JAVA = "java"
    public static final String OBJC = "objc"
    public static final String FOLDER_EXPLODED = 'exploded'
    public static final String FOLDER_OUT = 'out'
    public static final String FOLDER_J2OBJC_DEP_EXPLODED = 'j2objc'
    public static final String FOLDER_J2OBJC_ONLY_DEP_EXPLODED = 'j2objcOnly'
    public static final String FOLDER_TEST_J2OBJC_DEP_EXPLODED = 'testJ2objc'

    public static final String SOURCEPATH_OBJC_MAIN = "src/main/objc"
    public static final String SOURCEPATH_OBJC_TEST = "src/test/objc"

    private static J2objcInfo instance;
    public static final String J2OBJC_MAPPINGS = "j2objc.mappings"
    private final File buildDir

    public static final String TEST_CLASSES_LIST_FILENAME = "j2objcTests.txt"

    J2objcInfo(Project project){
        this(project.buildDir)
    }

    J2objcInfo(File buildDir) {
        this.buildDir = buildDir
    }

    @VisibleForTesting
    static J2objcInfo getInstance(File buildDir)
    {
        if(instance == null)
            instance = new J2objcInfo(buildDir)

        //Static value needs to be updated if changed. This whole class should not be static...
        if(!instance.buildDir.equals(buildDir))
            instance = new J2objcInfo(buildDir)
        return instance
    }

    static J2objcInfo getInstance(Project project)
    {
        return getInstance(project.buildDir)
    }

    @VisibleForTesting
    File rootBuildFile() {
        new File(buildDir, J2OBJC_BUILD)
    }

    File rootAssemblyFile()
    {
        new File(buildDir, J2OBJC_ASSEMBLY)
    }

    File dependencyBuildFile()
    {
        return new File(rootBuildFile(), DEPENDENCIES)
    }

    /**
     * Root folder for exploded dop archives
     * @param project
     * @return
     */
     File dependencyExplodedFile()
    {
        return new File(dependencyBuildFile(), FOLDER_EXPLODED)
    }

    /**
     * Exploded dir for 'j2objc' dependencies
     * @param project
     * @return
     */
     File dependencyExplodedJ2objcFile()
    {
        return new File(dependencyExplodedFile(), FOLDER_J2OBJC_DEP_EXPLODED)
    }

    /**
     * Exploded dir for 'j2objcOnly' dependencies
     * @param project
     * @return
     */
     File dependencyExplodedJ2objcOnlyFile()
    {
        return new File(dependencyExplodedFile(), FOLDER_J2OBJC_ONLY_DEP_EXPLODED)
    }

    /**
     * Exploded dir for 'testJ2objc' dependencies
     * @param project
     * @return
     */
     File dependencyExplodedTestJ2objcFile()
    {
        return new File(dependencyExplodedFile(), FOLDER_TEST_J2OBJC_DEP_EXPLODED)
    }

    File dependencyOutFile()
    {
        return new File(dependencyBuildFile(), FOLDER_OUT)
    }

    private File dependencyOutFileForPhase( String phase)
    {
        checkPhase(phase)
        return new File(dependencyOutFile(), phase)
    }

    File dependencyOutFileMain()
    {
        return dependencyOutFileForPhase(MAIN)
    }

    File dependencyOutFileTest()
    {
        return dependencyOutFileForPhase(TEST)
    }

    private void checkPhase(String phase) {
        if (!phase.equals(MAIN) && !phase.equals(TEST))
            throw new IllegalArgumentException("Phase must be main or test")
    }

    File sourceBuildFile()
    {
        return new File(rootBuildFile(), SOURCE)
    }

    File sourceBuildObjcFile()
    {
        return new File(sourceBuildFile(), OBJC)
    }

    File sourceBuildObjcFileForPhase( String phase)
    {
        checkPhase(phase)
        return new File(sourceBuildObjcFile(), phase)
    }

    File sourceBuildObjcFileMain()
    {
        return sourceBuildObjcFileForPhase(MAIN)
    }

    File sourceBuildObjcFileTest()
    {
        return sourceBuildObjcFileForPhase(TEST)
    }

    File sourceBuildOutFile()
    {
        return new File(sourceBuildFile(), FOLDER_OUT)
    }

    private File sourceBuildOutFileForPhase( String phase)
    {
        checkPhase(phase)
        return new File(sourceBuildOutFile(), phase)
    }

    File sourceBuildOutFileMain()
    {
        return sourceBuildOutFileForPhase(MAIN)
    }

    File sourceBuildOutFileTest()
    {
        return sourceBuildOutFileForPhase(TEST)
    }

    File dependencyOutMainMappings()
    {
        return new File(dependencyOutFileMain(), J2OBJC_MAPPINGS)
    }

    File dependencyOutTestMappings()
    {
        return new File(dependencyOutFileTest(), J2OBJC_MAPPINGS)
    }

    File sourceBuildOutMainMappings()
    {
        return new File(sourceBuildOutFileMain(), J2OBJC_MAPPINGS)
    }

    File sourceBuildOutTestMappings()
    {
        return new File(sourceBuildOutFileTest(), J2OBJC_MAPPINGS)
    }
}
