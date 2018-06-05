package org.j2objcgradle.gradle.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.internal.file.UnionFileTree
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternSet

import java.util.regex.Matcher


/**
 * Test task to run all unit tests and verify results.
 */
@CompileStatic
class TestTask extends DefaultTask {

    // *Test.java files and TestRunner binary
    @InputFile
    File testBinaryFile

    @InputFile
    File testPrefixesFile

    // 'Debug' or 'Release'
    @Input
    String buildType

    // Output required for task up-to-date checks
    @OutputFile
    File reportFile = project.file("${project.buildDir}/reports/${name}.out")

    @OutputDirectory
    // Combines main/test resources and test executables
    File getJ2objcTestDirFile() {
        assert buildType in ['Debug', 'Release']
        return new File(project.buildDir, "j2objcTest/$buildType")
    }


    @TaskAction
    void test() {
        Utils.requireMacOSX('j2objcTest task')

        Utils.projectCopy(project, {
            from testBinaryFile
            into getJ2objcTestDirFile()
        })

        // Test executable must be run from the same directory as the resources
        Utils.projectCopy(project, {
            from testPrefixesFile
            into getJ2objcTestDirFile()
        })


        File copiedTestBinary = new File(getJ2objcTestDirFile(), testBinaryFile.getName())
        logger.debug("Test Binary: $copiedTestBinary")

        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        ByteArrayOutputStream stderr = new ByteArrayOutputStream()

        // NOTE: last 's' is optional for the case of "OK (1 test)"
        // Capturing group is the test count, i.e. '\d+'
        String testCountRegex = /OK \((\d+) tests?\)/

        try {
            Utils.projectExec(project, stdout, stderr, testCountRegex, {
                executable copiedTestBinary
                args 'org.junit.runner.JUnitCore'

                FileTree javaSources = testSources.matching(new PatternSet().include("**/*.java"))
                FileTree javaSourcesWithTestAnnotation = javaSources.matching(new PatternSet().exclude(new Spec<FileTreeElement>() {
                    @Override
                    boolean isSatisfiedBy(FileTreeElement element) {
                        if (!element.isDirectory()) {
                            return !element.file.text.contains("org.junit.Test")
                        }
                        return false
                    }
                }))

                javaSourcesWithTestAnnotation.files.each { File testName ->
                    args fqcn(testName)
                }

                setStandardOutput stdout
                setErrorOutput stderr
            })

        } catch (Exception exception) {  // NOSONAR
            String message =
                    "The j2objcTest task failed. Given that the java plugin 'test' task\n" +
                    "completed successfully, this is an error specific to the J2ObjC Gradle\n" +
                    "Plugin build.\n" +
                    "\n" +
                    "1) Check BOTH 'Standard Output' and 'Error Output' above for problems.\n" +
                    "\n" +
                    "2) It could be that only the tests are failing while the non-test code\n" +
                    "may run correctly. If you can identify the failing test, then can try\n" +
                    "marking it to be ignored.\n" +
                    "\n" +
                    "To identify the failing test, look for 'Command Line failed' above.\n" +
                    "Copy and then run it in your shell. Selectively remove the test cases\n" +
                    "until you identify the failing test.\n" +
                    "\n" +
                    "Then the failing test can be filtered out using build.gradle:\n" +
                    "\n" +
                    "j2objcConfig {\n" +
                    "    testPattern {\n" +
                    "        exclude '**/FailingTest.java'\n" +
                    "        exclude 'src/main/java/Package/FailingDirectory/**'\n" +
                    "    }\n" +
                    "}\n" +
                    "\n" +
                    "Look at known J2ObjcC crash issues for further insights:\n" +
                    "    https://github.com/google/j2objc/issues?q=is%3Aissue+is%3Aopen+crash\n"

            // Copy message afterwards to make it more visible as exception text may be long
            message = exception.toString() + '\n' + message
            throw new InvalidUserDataException(message, exception)
        }

        // Only write output if task is successful
        reportFile.write(Utils.stdOutAndErrToLogString(stdout, stderr))
        logger.debug("Test Output: ${reportFile.path}")

        String testCountStr = Utils.matchRegexOutputs(stdout, stderr, testCountRegex)
        if (!testCountStr?.isInteger()) {
            // Should have been caught in projectExec call above
            throw new InvalidUserDataException(
                    Utils.stdOutAndErrToLogString(stdout, stderr) + '\n' +
                    'Tests passed but could not find test count.\n' +
                    'Failed Regex Match testCountRegex: ' +
                    Utils.escapeSlashyString(testCountRegex) + '\n' +
                    'Found: ' + testCountStr)
        }
        int testCount = testCountStr.toInteger()

        String message =
                "\n" +
                "j2objcConfig {\n" +
                "    testMinExpectedTests ${testCount}\n" +
                "}\n"
        if (testCount == 0) {
            message =
                        "No unit tests were run. Unit tests are strongly encouraged with J2ObjC.\n" +
                        "J2ObjC build of project '${project.name}'\n" +
                        "\n" +
                        "To disable this check (against best practice), modify build.gradle:\n" +
                        message

            throw new InvalidUserDataException(message)
        }

        logger.lifecycle "Executed $testCount tests."
    }

    static String fqcn(File file) {
        String content = file.text
        def packageName = ""
        Matcher matcher = (content =~ /package (.*);/)
        if (matcher.size() > 0) {
            List packageNameMatch = matcher[0] as List
            packageName = packageNameMatch.get(1)
        }
        def className = file.name.replaceAll(".java", "")

        return "$packageName.$className"
    }

    FileTree sources = new UnionFileTree()

    def testSources(List<FileTree> sources) {
        sources.each {
            this.sources += it
        }

    }

    @InputFiles
    FileTree getTestSources() {
        sources
    }
}
