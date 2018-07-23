package org.j2objcgradle.gradle.tasks

import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.process.ExecSpec

class BaseTranslateTask extends BaseChangesTask {

    Set<Configuration> classpath = []

    @InputFiles
    Collection<File> getClasspathFiles() {
        Set<File> files = []
        classpath.each {
            files.addAll( it.resolvedConfiguration.files )
        }
        return files
    }

    def classpath(Configuration... dependency) {
        dependency.each { Configuration it ->
            classpath.add it
        }
    }

    def configureClasspathArg(ExecSpec spec) {
        //Classpath arg for translation. Includes user specified jars, j2objc 'standard' jars, and j2objc dependency libs
        UnionFileCollection classpathFiles = new UnionFileCollection([
                project.files(Utils.j2objcLibs(getJ2objcHome(), getTranslateJ2objcLibs()))
        ])

        classpathFiles += getClasspathFiles()

        String classpathArg = Utils.joinedPathArg(classpathFiles)

        if(!classpathArg.isEmpty()) {
            spec.args "-classpath", classpathArg
        }
    }

}
