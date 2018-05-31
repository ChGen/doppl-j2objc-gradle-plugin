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

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSet
import org.j2objcgradle.gradle.J2objcConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.j2objcgradle.gradle.FrameworkConfig

class PodspecWriterTask extends DefaultTask {

    @Input
    String sourceSetName

    @Nested
    FrameworkConfig config

    @InputFiles
    List<File> headers = new ArrayList<>()

    @OutputFile
    File getPodspec() {
        return new File(project.projectDir, podSpecFileName())
    }

    final String podSpecFileName() {
        "${podName()}.podspec"
    }

    final String podName() {
        "${J2objcConfig.from(project).podName}${(sourceSetName == SourceSet.TEST_SOURCE_SET_NAME ? "Test" : "")}"
    }

    void headers(File... header) {
        this.headers.addAll(header)
    }

    @TaskAction
    void writePodspec() {
        String podspecTemplate = config.podspecTemplate(
                project,
                headers,
                podName())

        podspec.text = podspecTemplate
    }
}
