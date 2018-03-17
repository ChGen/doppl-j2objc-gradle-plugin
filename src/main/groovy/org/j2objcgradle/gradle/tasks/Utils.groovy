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

import com.google.common.annotations.VisibleForTesting
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Slf4j
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Nullable
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.collections.DefaultConfigurableFileTree
import org.gradle.api.tasks.WorkResult
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.process.internal.ExecException
import org.gradle.util.GradleVersion
import org.j2objcgradle.gradle.helper.J2objcRuntimeHelper

import java.util.regex.Matcher

/**
 * Internal utilities supporting plugin implementation.
 */
// Without access to the project, logging is performed using the
// static 'log' variable added during decoration with this annotation.
@Slf4j
//@CompileStatic
class Utils {
    private Utils() {}

    static boolean failGradleVersion(boolean throwIfUnsupported) {
        return failGradleVersion(GradleVersion.current(), throwIfUnsupported)
    }

    @VisibleForTesting
    static boolean failGradleVersion(GradleVersion gradleVersion, boolean throwIfUnsupported) {
        String errorMsg = ''

        final GradleVersion minGradleVersion = GradleVersion.version('2.8')
        if (gradleVersion.compareTo(minGradleVersion) < 0) {
            errorMsg = "J2ObjC Gradle Plugin requires minimum Gradle version: $minGradleVersion"
        }

        if (!errorMsg.isEmpty()) {
            if (throwIfUnsupported) {
                throw new InvalidUserDataException(errorMsg)
            } else {
                return true
            }
        }
        return false
    }

    static List<Integer> parseVersionComponents(String ver) {
        return ver.tokenize('.').collect({ String versionComponent ->
            try {
                return Integer.parseInt(versionComponent)
            } catch (NumberFormatException nfe) {
                // Keep it simple.  If the version provided doesn't meet a simple N.N.N format,
                // assume the user knows what they are doing and keep going.  The maximum integer
                // provides this behavior.
                return Integer.MAX_VALUE
            }
        })
    }

    static boolean stringEquals(String a, String b)
    {
        if(a != null && b != null)
            return a.equals(b)

        return a == null && b == null
    }

    static boolean isAtLeastVersion(String version, String minVersion) {
        List<Integer> minVersionComponents = parseVersionComponents(minVersion)
        List<Integer> versionComponents = parseVersionComponents(version)
        for (int i = 0; i < Math.min(minVersionComponents.size(), versionComponents.size()); i++) {
            if (versionComponents[i] > minVersionComponents[i]) {
                return true
            } else if (versionComponents[i] < minVersionComponents[i]) {
                return false
            }
        }
        // Each existing component was equal.  If the requested version is at least as long, we're good.
        return versionComponents.size() >= minVersionComponents.size()
    }

    static String getLowerCaseOSName() {
        return System.getProperty('os.name').toLowerCase()
    }

    static boolean isLinux() {
        String osName = getLowerCaseOSName()
        // http://stackoverflow.com/a/18417382/1509221
        return osName.contains('nux')
    }

    static boolean isMacOSX() {
        String osName = getLowerCaseOSName()
        // http://stackoverflow.com/a/18417382/1509221
        return osName.contains('mac') || osName.contains('darwin')
    }

    static boolean isWindows() {
        String osName = getLowerCaseOSName()
        return osName.contains('windows')
    }

    // Add valid keys here
    // Use camelCase and order alphabetically
    private static final List<String> PROPERTIES_VALID_KEYS =
            Collections.unmodifiableList(Arrays.asList(
                    'debug.enabled',
                    'enabledArchs',
                    'home',
                    'release.enabled',
                    'translateOnlyMode'
            ))

    private static final String PROPERTY_KEY_PREFIX = 'j2objc.'

    /**
     * Retrieves the local properties with highest precedence:
     * 1.  local.properties value like j2objc.name1.name2 when present.
     * 2.  environment variable like J2OBJC_NAME1_NAME2 when present.
     * 3.  defaultValue.
     */
    static String getLocalProperty(Project proj, String key, String defaultValue = null) {

        // Check for requesting invalid key
        if (!(key in PROPERTIES_VALID_KEYS)) {
            throw new InvalidUserDataException(
                    "Requesting invalid property: $key\n" +
                    "Valid Keys: $PROPERTIES_VALID_KEYS")
        }
        File localPropertiesFile = new File(proj.rootDir, 'local.properties')
        String result = null
        if (localPropertiesFile.exists()) {
            Properties localProperties = new Properties()
            localPropertiesFile.withInputStream {
                localProperties.load it
            }

            // Check valid key in local.properties for everything with PROPERTY_KEY_PREFIX
            localProperties.keys().each { String propKey ->
                if (propKey.startsWith(PROPERTY_KEY_PREFIX)) {
                    String withoutPrefix =
                            propKey.substring(PROPERTY_KEY_PREFIX.length(), propKey.length())
                    if (!(withoutPrefix in PROPERTIES_VALID_KEYS)) {
                        throw new InvalidUserDataException(
                                "Invalid J2ObjC Gradle Plugin property: $propKey\n" +
                                "From local.properties: $localPropertiesFile.absolutePath\n" +
                                "Valid Keys: $PROPERTIES_VALID_KEYS")
                    }
                }
            }

            result = localProperties.getProperty(PROPERTY_KEY_PREFIX + key, null)
        }
        if (result == null) {
            // debug.enabled becomes J2OBJC_DEBUG_ENABLED
            String envName = 'J2OBJC_' + key.replace('.', '_').toUpperCase(Locale.ENGLISH)
            // TODO: Unit tests.
            result = System.getenv(envName)
        }
        return result == null ? defaultValue : result
    }

    static String j2objcDeclaredVersion(Project project)
    {
        if(project.hasProperty("j2objc_runtime"))
        {
            return project.findProperty("j2objc_runtime").toString()
        }
        else
        {
            return null
        }
    }

    static String j2objcHome(Project proj) {

        String version = Utils.j2objcDeclaredVersion(proj)
        if (version != null) {
            return J2objcRuntimeHelper.runtimeDir(version).canonicalPath
        } else {
            return new File(j2objcLocalHomeOrNull(proj)).absolutePath
        }
    }

    static String j2objcLocalHomeOrNull(Project proj)
    {
        String j2objcHome = getLocalProperty(proj, 'home')
        if (j2objcHome == null) {
            j2objcHome = System.getenv("J2OBJC_HOME")
        }
        return j2objcHome
    }

    public static String findVersionString(Project project, String j2objcHome) {
        String j2objcExecutable = "$j2objcHome/j2objc"

        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        ByteArrayOutputStream stderr = new ByteArrayOutputStream()

        project.logger.debug('VerifyJ2objcRequirements - projectExec:')
        try {
            projectExec(project, stdout, stderr, null, {
                executable j2objcExecutable

                // Arguments
                args "-version"

                setStandardOutput stdout
                setErrorOutput stderr
            })

        } catch (Exception exception) {
            throw new RuntimeException(exception.toString() + "\n\n" +
                                       "J2ObjC binary at $j2objcHome failed version call.", exception)
        }
        // Yes, J2ObjC uses stderr to output the version.
        String actualVersionString = stderr.toString().trim()
        if(actualVersionString.startsWith("j2objc "))
            actualVersionString = actualVersionString.substring("j2objc ".length())
        return actualVersionString
    }

    // Provides a subset of "args" interface from project.exec as implemented by ExecHandleBuilder:
    // https://github.com/gradle/gradle/blob/master/subprojects/core/src/main/groovy/org/gradle/process/internal/ExecHandleBuilder.java
    // Allows the following:
    // j2objcConfig {
    //     translateArgs '--no-package-directories', '--prefixes', 'prefixes.properties'
    // }
    @VisibleForTesting
    static void appendArgs(List<String> listArgs, String nameArgs, boolean rejectSpaces, String... args) {
        verifyArgs(nameArgs, rejectSpaces, args)
        listArgs.addAll(Arrays.asList(args))
    }

    // Verify that no argument contains a space
    @VisibleForTesting
    static void verifyArgs(String nameArgs, boolean rejectSpaces, String... args) {
        if (args == null) {
            throw new InvalidUserDataException("$nameArgs == null!")
        }
        for (String arg in args) {
            if (arg.isAllWhitespace()) {
                throw new InvalidUserDataException(
                        "$nameArgs is all whitespace: '$arg'")
            }
            if (rejectSpaces) {
                if (arg.contains(' ')) {
                    String rewrittenArgs = "'" + arg.split(' ').join("', '") + "'"
                    throw new InvalidUserDataException(
                            "'$arg' argument should not contain spaces and be written out as distinct entries:\n" +
                            "$nameArgs $rewrittenArgs")
                }
            }
        }
    }

    // Add list of java path to a FileCollection as a FileTree
    static List<ConfigurableFileTree> javaTrees(Project proj, List<String> treePaths) {
        List<ConfigurableFileTree> trees =
            treePaths.collect({ String treePath -> proj.fileTree(dir: treePath, includes: ["**/*.java"]) })
        return trees
    }

    static File dirFromFileTree(FileTree fileTree)
    {
        if(fileTree instanceof DefaultConfigurableFileTree)
        {
            return ((DefaultConfigurableFileTree)fileTree).getDir()
        }
        return null
    }

    static List<String> j2objcLibs(String j2objcHome,
                                   List<String> libraries) {
        return libraries.collect { String library ->
            return "$j2objcHome/lib/$library"
        }
    }

    public static boolean isJavaTypeProject(Project project){
        return hasOneOfTheFollowingPlugins(project, 'java');
    }

    public static boolean isAndroidTypeProject(Project project){
        return hasOneOfTheFollowingPlugins(
                project,
                "com.android.application",
                "com.android.feature",
                "android",
                "com.android.test",
                "android-library",
                "com.android.library");
    }

    public static boolean hasOneOfTheFollowingPlugins(Project project, String... pluginIds)
    {
        for (String pluginId : pluginIds) {
            if(project.plugins.hasPlugin(pluginId))
                return true
        }

        return false
    }

    // Convert FileCollection to joined path arg, e.g. "src/Some.java:src/Another.java"
    static String joinedPathArg(FileCollection files) {
        String[] paths = []
        files.each { File file ->
            paths += file.path
        }
        // OS specific separator, i.e. ":" on OS X and ";" on Windows
        return paths.join(File.pathSeparator)
    }

    static String joinedPathArg(Collection<File> files) {
        String[] paths = []
        files.each { File file ->
            paths += file.path
        }
        // OS specific separator, i.e. ":" on OS X and ";" on Windows
        return paths.join(File.pathSeparator)
    }

    // Convert regex to string for display, wrapping it with /.../
    // From Groovy-Lang: "Only forward slashes need to be escaped with a backslash"
    // http://docs.groovy-lang.org/latest/html/documentation/#_slashy_string
    static String escapeSlashyString(String regex) {
        return '/' + regex.replace('/', '\\/') + '/'
    }

    static String relativePath(File parent, File target)
    {
        String parentPath = parent.getPath()
        if(target.getPath().startsWith(parentPath))
        {
            if(!parentPath.endsWith(File.separator))
                parentPath += File.separator

            return target.getPath().substring(parentPath.length())
        }
        else {
            return target.getPath()
        }
    }

    // Matches regex, return first match as string, must have >1 capturing group
    // Return first capture group, comparing stderr first then stdout
    // Returns null for no match
    static String matchRegexOutputs(
            ByteArrayOutputStream stdout,
            ByteArrayOutputStream stderr,
            @Nullable String regex) {

        if (regex == null) {
            return null
        }

        Matcher stdMatcher = (stdout.toString() =~ regex)
        Matcher errMatcher = (stderr.toString() =~ regex)
        // Requires a capturing group in the regex
        String assertFailMsg =
                "matchRegexOutputs must have '(...)' capture group, regex: " +
                escapeSlashyString(regex)
        assert stdMatcher.groupCount() >= 1, assertFailMsg
        assert errMatcher.groupCount() >= 1, assertFailMsg

        if (errMatcher.find()) {
            return errMatcher.group(1)
        }
        if (stdMatcher.find()) {
            return stdMatcher.group(1)
        }

        return null
    }

    static Properties propsFromStringMap(Map<String, String> map)
    {
        Properties properties = new Properties()
        for (String key : map.keySet()) {
            properties.put(key, map.get(key))
        }

        return properties
    }

    @VisibleForTesting
    static String projectExecLog(
            ExecSpec execSpec, ByteArrayOutputStream stdout, ByteArrayOutputStream stderr,
            boolean execSucceeded, Exception exception) {
        // Add command line and stderr to make the error message more useful
        // Chain to the original ExecException for complete stack trace

        String msg
        // The command line can be long, so highlight more important details below
        if (execSucceeded) {
            msg = 'Command Line Succeeded:\n'
        } else {
            msg = 'Command Line Failed:\n'
        }

        msg += execSpec.getCommandLine().join(' ') + '\n'

        // Working Directory appears to always be set
        if (execSpec.getWorkingDir() != null) {
            msg += 'Working Dir:\n'
            msg += execSpec.getWorkingDir().absolutePath + '\n'
        }

        // Use 'Cause' instead of 'Caused by' to help distinguish from exceptions
        if (exception != null) {
            msg += 'Cause:\n'
            msg += exception.toString() + '\n'
        }

        // Stdout and stderr
        msg += stdOutAndErrToLogString(stdout, stderr)
        return msg
    }

    static String stdOutAndErrToLogString(ByteArrayOutputStream stdout, ByteArrayOutputStream stderr) {
        return 'Standard Output:\n' +
                stdout.toString() + '\n' +
                'Error Output:\n' +
                stderr.toString()
    }

    static boolean isProjectExecNonZeroExit(Exception exception) {
        return (exception instanceof InvalidUserDataException) &&
               // TODO: improve indentification of non-zero exits?
               (exception?.getCause() instanceof ExecException)
    }

    /**
     * Copy content to directory by calling project.copy(closure)
     *
     * Must be called instead of project.copy(...) to allow mocking of project calls in testing.
     *
     * @param proj Calls proj.copy {...} method
     * @param closure CopySpec closure
     */
    // See projectExec for explanation of the code
    @CompileStatic(TypeCheckingMode.SKIP)
    static WorkResult projectCopy(Project proj,
                                  @ClosureParams(value = SimpleType.class, options = "org.gradle.api.file.CopySpec")
                                  @DelegatesTo(CopySpec)
                                          Closure closure) {
        proj.copy {
            (delegate as CopySpec).with closure
        }
    }

    private static ThreadLocal<byte []> copyBufferLocation = new ThreadLocal<byte []>(){
        @Override
        protected byte [] initialValue() {
            return new byte[2048]
        }
    }

    /**
     * Executes command line and returns result by calling project.exec(...)
     *
     * Throws exception if command fails or non-null regex doesn't match stdout or stderr.
     * The exceptions have detailed information on command line, stdout, stderr and failure cause.
     * Must be called instead of project.exec(...) to allow mocking of project calls in testing.
     *
     * @param proj Calls proj.exec {...} method
     * @param stdout To capture standard output
     * @param stderr To capture standard output
     * @param matchRegexOutputsRequired Throws exception if stdout/stderr don't match regex.
     *        Matches each OutputStream separately, not in combination. Ignored if null.
     * @param closure ExecSpec type for proj.exec {...} method
     * @return ExecResult from the method
     */
    // See http://melix.github.io/blog/2014/01/closure_param_inference.html
    //
    // TypeCheckingMode.SKIP allows Project.exec to be mocked via metaclass in TestingUtils.groovy.
    // ClosureParams allows type checking to enforce that the first param ('it') to the Closure is
    // an ExecSpec. DelegatesTo allows type checking to enforce that the delegate is ExecSpec.
    // Together this emulates the functionality of ExecSpec.with(Closure).
    //
    // We are using a non-API-documented assumption that the delegate is an ExecSpec.  If the
    // implementation changes, this will fail at runtime.
    // TODO: In Gradle 2.5, we can switch to strongly-typed Actions, like:
    // https://docs.gradle.org/2.5/javadoc/org/gradle/api/Project.html#copy(org.gradle.api.Action)
    @CompileStatic(TypeCheckingMode.SKIP)
    static ExecResult projectExec(
            Project proj,
            OutputStream stdout,
            OutputStream stderr,
            @Nullable String matchRegexOutputsRequired,
            @ClosureParams(value = SimpleType.class, options = "org.gradle.process.ExecSpec")
            @DelegatesTo(ExecSpec)
                    Closure closure) {

        ExecSpec execSpec = null
        ExecResult execResult
        boolean execSucceeded = false

        try {
            execResult = proj.exec {
                execSpec = delegate as ExecSpec
                (execSpec).with closure
            }
            execSucceeded = true
            if (matchRegexOutputsRequired) {
                if (!matchRegexOutputs(stdout, stderr, matchRegexOutputsRequired)) {
                    // Exception thrown here to output command line
                    throw new InvalidUserDataException(
                            'Unable to find expected expected output in stdout or stderr\n' +
                            'Failed Regex Match: ' + escapeSlashyString(matchRegexOutputsRequired))
                }
            }

        } catch (Exception exception) {  // NOSONAR
            // ExecException is most common, which indicates "non-zero exit"
            String exceptionMsg = projectExecLog(execSpec, stdout, stderr, execSucceeded, exception)
            throw new InvalidUserDataException(exceptionMsg, exception)
        }

        log.debug(projectExecLog(execSpec, stdout, stderr, execSucceeded, null))

        return execResult
    }

    // Max number of characters for OS command line
    static int maxArgs() {
        if (isMacOSX()) {
            // http://www.in-ulm.de/~mascheck/various/argmax/
            return 262144
        }
        if (isWindows()) {
            // Assume Windows XP or later which has max of 8191
            // https://support.microsoft.com/en-us/kb/830473
            return 8191
        }
        if (isLinux()) {
            // v2.6.23 (released 2007) or later limit is 1/4 of stack size,
            // so Linux is presumed to have no limit
            // http://www.in-ulm.de/~mascheck/various/argmax/
            return Integer.MAX_VALUE
        }
        assert false
    }
}
