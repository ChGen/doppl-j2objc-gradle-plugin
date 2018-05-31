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

import org.gradle.api.tasks.Input
import org.j2objcgradle.gradle.tasks.Utils
import org.gradle.api.Project


class FrameworkConfig {
    private static final String SOURCE_EXTENSIONS = "h,m,cpp,properites,txt"

    @Input
    String homepage = "http://j2objc.org/"

    @Input
    String license = "{ :type => 'Apache 2.0' }"

    @Input
    String authors = "{ 'Filler Person' => 'filler@example.com' }"

    @Input
    String source = "{ :git => 'https://github.com/google/j2objc.git'}"

    @Input
    boolean writeActualJ2objcPath = true

    @Input
    String iosDeploymentTarget = "8.0"

    @Input
    boolean flagObjc = true

    @Input
    boolean libZ = true

    @Input
    boolean libSqlite3 = true

    @Input
    boolean libIconv = true

    @Input
    boolean libJre_emul = true

    @Input
    List<String> managedPodsList = new ArrayList<>()

    void managePod(String... paths)
    {
        for (String p : paths) {
            managedPodsList.add(p)
        }
    }

    @Input
    List<String> addLibraries = new ArrayList<>()

    void addLibraries(String... libs)
    {
        for (String l : libs) {
            this.addLibraries.add(l)
        }
    }

    boolean frameworkUIKit = true

    @Input
    List<String> addFrameworks = new ArrayList<>()

    void addFrameworks(String... frameworks)
    {
        for (String f : frameworks) {
            this.addFrameworks(f)
        }
    }

    String writeLibs()
    {
        List<String> allLibs = new ArrayList<>(addLibraries)
        if(libZ)allLibs.add("z")
        if(libSqlite3)allLibs.add("sqlite3")
        if(libIconv)allLibs.add("iconv")
        if(libJre_emul)allLibs.add("jre_emul")

        return "'"+ allLibs.join("', '") +"'"
    }

    String writeFrameworks()
    {
        List<String> allFrameworks = new ArrayList<>(addFrameworks)
        if(frameworkUIKit)allFrameworks.add("UIKit")

        return "'"+ allFrameworks.join("', '") +"'"
    }

    String podspecTemplate (
            Project project,
            List<File> headers,
            String podname){

        String j2objcPath = writeActualJ2objcPath ? Utils.j2objcHome(project) : "\$(J2OBJC_LOCAL_PATH)"

        String headerPaths = headers
                .collect({ "\"${J2objcPlugin.POD_TARGET_SRC_DIR}/$it.name\"" })
                .join(", ")

        String objcFlagString = flagObjc ? "'OTHER_LDFLAGS' => '-ObjC'," : ""

        return"""

# Generated by the J2Objc gradle plugin 

Pod::Spec.new do |s|

    s.name             = '${podname}'
    s.version          = '0.1.0'
    s.summary          = 'J2objc code framework'

    s.description      = <<-DESC
  TODO: Add long description of the pod here.
                         DESC

    s.homepage         = '${homepage}'
    s.license          = ${license}
    s.authors          = ${authors}
    s.source           = ${source}

    s.ios.deployment_target = '${iosDeploymentTarget}'

    s.source_files = "src/**/*.{h,m,cpp}"

    s.public_header_files = ${headerPaths}

    s.header_mappings_dir = "src"
    s.requires_arc = false
    s.libraries = 'z', 'sqlite3', 'iconv', 'jre_emul'
    s.frameworks = 'UIKit'

    s.pod_target_xcconfig = {
     'HEADER_SEARCH_PATHS' => '${j2objcPath}/include',
     'LIBRARY_SEARCH_PATHS' => '${j2objcPath}/lib',
     $objcFlagString
     'CLANG_WARN_DOCUMENTATION_COMMENTS' => 'NO',
     'GCC_WARN_64_TO_32_BIT_CONVERSION' => 'NO'
    }
    
    s.user_target_xcconfig = {
     'HEADER_SEARCH_PATHS' => '${j2objcPath}/frameworks/JRE.framework/Headers'
    }
    
    
    
end
"""
    }
}
