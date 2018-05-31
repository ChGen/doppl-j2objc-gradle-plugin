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

import org.j2objcgradle.gradle.J2objcConfig
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.UnionFileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet

class ListTestsTask extends BaseChangesTask {
    List<FileTree> sourceSets

    @OutputFile
    File output

    @InputFiles
    FileCollection getSrcFiles() {
        sourceSets = _buildContext.getBuildTypeProvider().testSourceSets(project)
        return locateTestFiles(sourceSets)
    }

    private FileCollection locateTestFiles(List<FileTree> sourceDirs) {

        FileTree allFiles = new UnionFileTree("testClasses", (Collection<? extends FileTree>) sourceDirs)

        J2objcConfig j2objcConfig = J2objcConfig.from(project)

        PatternSet testIdentifier

        if (j2objcConfig.testIdentifier == null) {
            testIdentifier = new PatternSet().include("**/*Test.java")
        } else {
            testIdentifier = j2objcConfig.testIdentifier
        }

        FileCollection resultCollection = allFiles.matching(testIdentifier)

        if (j2objcConfig.translatePattern != null) {
            resultCollection = resultCollection.matching(j2objcConfig.translatePattern)
        }

        return resultCollection
    }

    @TaskAction
    void writeTestList() {

        if(J2objcConfig.from(project).skipTests)
            return

        // Don't evaluate this expensive property multiple times.
        FileCollection originalSrcFiles = getSrcFiles()

        List<String> classes = new ArrayList<>()
        String filepath

        for (File file : originalSrcFiles) {
            for (FileTree ft : sourceSets) {
                def temp = processFileTree(file, ft)
                if (temp != null)
                    filepath = temp
            }

            if (filepath != null)
                classes.add(filepath)
        }

        output.write(String.join("\n", classes))
    }

    private String processFileTree(File file, FileTree tree) {
        String filepath
        String treeDir = tree.dir.toString()

        if (file.path.startsWith(treeDir)) {
            filepath = file.path - "$treeDir/"
            filepath = filepath.replace('/', '.')
            filepath = filepath - ".java"
        }

        return filepath
    }
}
