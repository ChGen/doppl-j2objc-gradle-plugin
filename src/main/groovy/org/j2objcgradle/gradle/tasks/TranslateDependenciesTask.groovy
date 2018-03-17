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
import org.j2objcgradle.gradle.J2objcDependency
import org.j2objcgradle.gradle.J2objcInfo
import org.j2objcgradle.gradle.J2objcVersionManager
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.internal.file.UnionFileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.j2objcgradle.gradle.DependencyResolver

class TranslateDependenciesTask extends BaseChangesTask{

    boolean testBuild

    @Input boolean isDependenciesEmitLineDirectives() {
        J2objcConfig.from(project).dependenciesEmitLineDirectives
    }

    @Input
    String getDependencyVersions()
    {
        DependencyResolver resolver = _buildContext.getDependencyResolver()
        List<J2objcDependency> libs = dependencyList(resolver)

        return flattenLibs(libs)
    }

    @OutputDirectory
    File getBuildOut() {
        if(testBuild)
            return J2objcInfo.getInstance(project).dependencyOutFileTest()
        else
            return J2objcInfo.getInstance(project).dependencyOutFileMain()
    }

    private List<J2objcDependency> dependencyList(DependencyResolver resolver) {
        return testBuild ? resolver.translateJ2objcTestLibs : resolver.translateJ2objcLibs
    }

    //TODO: This assumes the folders are distinct. Need a better solution.
    private String flattenLibs(List<J2objcDependency> libs)
    {
        List<String> parts = new ArrayList<>()
        for (J2objcDependency dep : libs) {
            parts.add(dep.dependencyFolderLocation().name)
        }

        return parts.join("|")
    }

    File getMappingsFile()
    {
        J2objcInfo j2objcInfo = J2objcInfo.getInstance(project)
        if(testBuild)
            return j2objcInfo.dependencyOutTestMappings()
        else
            return j2objcInfo.dependencyOutMainMappings()
    }

    @TaskAction
    void translateDependencies(IncrementalTaskInputs inputs) {

        J2objcVersionManager.checkJ2objcConfig(project, true)

        String j2objcExecutable = "${getJ2objcHome()}/j2objc"

        J2objcInfo j2objcInfo = J2objcInfo.getInstance(project)


        List<J2objcDependency> dependencies = dependencyList(_buildContext.getDependencyResolver())

        if (dependencies.size() == 0)
            return

        UnionFileCollection classpathFiles = new UnionFileCollection([
                project.files(Utils.j2objcLibs(getJ2objcHome(), getTranslateJ2objcLibs()))
        ])

        String classpathArg = Utils.joinedPathArg(classpathFiles)

        List<File> sourcepathList = new ArrayList<>()

        if (testBuild) {
            ArrayList<J2objcDependency> libs = _buildContext.getDependencyResolver().translateJ2objcLibs
            for (J2objcDependency dep : libs) {
                sourcepathList.add(dep.dependencyJavaFolder())
            }
        }

        Map<String, String> allPrefixes = getPrefixes()

        runTranslate(j2objcExecutable, j2objcInfo, sourcepathList, classpathArg, allPrefixes, dependencies)
    }

    private void runTranslate(String j2objcExecutable, J2objcInfo j2objcInfo, List<File> sourcepathList, String classpathArg,
                              allPrefixes, List<J2objcDependency> dependencies) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        ByteArrayOutputStream stderr = new ByteArrayOutputStream()

        UnionFileTree fileTree = new UnionFileTree("All Dependency Java")

        for (J2objcDependency dep : dependencies) {
            fileTree.add(project.fileTree(dir: dep.dependencyJavaFolder(), includes: ["**/*.java"]))
        }

        File buildOut = getBuildOut()
        buildOut.mkdirs()
        File javaBatch = new File(buildOut, "javabatch.in")

        javaBatch.write(fileTree.files.join("\n"))

        try {
            Utils.projectExec(project, stdout, stderr, null, {
                executable j2objcExecutable

                // Arguments
                args "-d", Utils.relativePath(project.projectDir, buildOut)
                args "-XcombineJars", ''
                args "-XglobalCombinedOutput", "${testBuild ? 'test' : 'main'}DependencyOut"
                args "--swift-friendly", ''
                args "--output-header-mapping", getMappingsFile().path

                if (isDependenciesEmitLineDirectives()) {
                    args "-g", ''
                }

                if (sourcepathList.size() > 0) {
                    args "-sourcepath", Utils.joinedPathArg(sourcepathList)
                }

                if (testBuild) {
                    File mappingsFile = j2objcInfo.sourceBuildOutMainMappings()
                    if (mappingsFile.exists()) {
                        args "--header-mapping", mappingsFile.path
                    }
                }

                if(!classpathArg.isEmpty()) {
                    args "-classpath", classpathArg
                }

                getTranslateArgs().each { String translateArg ->
                    args translateArg
                }

                allPrefixes.keySet().each { String packageString ->
                    args "--prefix", packageString + "=" + allPrefixes.get(packageString)
                }

                args "@${Utils.relativePath(project.projectDir, javaBatch)}"

                setStandardOutput stdout
                setErrorOutput stderr

                setWorkingDir project.projectDir
            })

        } catch (Exception exception) {  // NOSONAR
            // TODO: match on common failures and provide useful help
            throw exception
        }

//        javaBatch.delete()
    }
}
