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

package org.j2objcgradle.gradle.tasks

import org.j2objcgradle.gradle.BuildContext
import org.j2objcgradle.gradle.J2objcConfig
import org.j2objcgradle.gradle.J2objcInfo
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.UnionFileTree
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.j2objcgradle.gradle.BuildTypeProvider

/**
 * Copies artifacts into j2objc directory structure
 */

class J2objcAssemblyTask extends DefaultTask {

    BuildContext _buildContext

    @InputFiles
    FileCollection getInputFiles()
    {
        FileTree fileTree = new UnionFileTree(getClass().getSimpleName())

        BuildTypeProvider buildTypeProvider = _buildContext.getBuildTypeProvider()

        List<FileTree> sets = buildTypeProvider.sourceSets(project)
        for (FileTree set : sets) {
            fileTree.addToUnion(set)
        }

        J2objcConfig j2objcConfig = J2objcConfig.from(project)
        if(j2objcConfig.translatePattern != null) {
            fileTree = fileTree.matching(j2objcConfig.translatePattern)
        }

        fileTree = fileTree.matching(TranslateTask.javaPattern {
            include "**/*.java"
        })

        return fileTree
    }

    @InputDirectory @Optional
    File getObjcDir(){
        File f = project.file(J2objcInfo.SOURCEPATH_OBJC_MAIN)
        return f.exists() ? f : null
    }

    @OutputDirectory
    File getDestDirFile() {
        return J2objcInfo.getInstance(project).rootAssemblyFile()
    }

    @TaskAction
    void j2objcAssembly(IncrementalTaskInputs inputs) {

        Utils.projectCopy(project, {
            from getInputFiles()
            into new File(getDestDirFile(), "java")
            include '**/*.java'
        })

        File objcDir = getObjcDir()
        if (objcDir != null) {
            Utils.projectCopy(project, {
                from objcDir
                into new File(getDestDirFile(), "src")
            })
        }

    }
}
