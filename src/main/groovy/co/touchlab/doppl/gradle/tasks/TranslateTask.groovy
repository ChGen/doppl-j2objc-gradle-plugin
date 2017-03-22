/*

 */

package co.touchlab.doppl.gradle.tasks

import co.touchlab.doppl.gradle.BuildContext
import co.touchlab.doppl.gradle.DopplConfig
import co.touchlab.doppl.gradle.DopplDependency
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.UnionFileCollection
import org.gradle.api.internal.file.UnionFileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.incremental.InputFileDetails
import org.gradle.api.tasks.util.PatternSet

/**
 * Translation task for Java to Objective-C using j2objc tool.
 */
@CompileStatic
class TranslateTask extends BaseChangesTask {

    // Main java source files. May be generated by other tasks.
    @InputFiles
    FileCollection getMainSrcFiles() {
        List<FileTree> sourceSets = _buildContext.getBuildTypeProvider().sourceSets(project)
        return replaceOverlayFilterJava(sourceSets)
    }

    // Test java source files. May be generated by other tasks.
    @InputFiles
    FileCollection getTestSrcFiles() {
        List<FileTree> sourceSets = _buildContext.getBuildTypeProvider().testSourceSets(project)
        return replaceOverlayFilterJava(sourceSets)
    }

    @Input
    Map<String, String> getTranslateSourceMapping() { return DopplConfig.from(project).translateSourceMapping }

    @InputDirectory @Optional
    File srcMainObjcDir;

    @InputDirectory @Optional
    File srcTestObjcDir;

    // Generated ObjC files
    @OutputDirectory
    File srcGenMainDir

    @OutputDirectory
    File srcGenTestDir

    //This is sort of an input, but should be covered by source file inputs
    Set<File> getMainSrcDirs() {
        Set<File> allFiles = new HashSet<>()
        for (FileTree genPath : _buildContext.getBuildTypeProvider().sourceSets(project)) {
            allFiles.add(Utils.dirFromFileTree(genPath))
        }
        return allFiles
    }

    //This is sort of an input, but should be covered by source file inputs
    Set<File> getTestSrcDirs() {
        Set<File> allFiles = new HashSet<>()
        allFiles.addAll(getMainSrcDirs())

        for (FileTree genPath : _buildContext.getBuildTypeProvider().testSourceSets(project)) {
            allFiles.add(Utils.dirFromFileTree(genPath))
        }

        return allFiles
    }

    List<DopplDependency> getTranslateDopplLibs() { return _buildContext.getDependencyResolver().translateDopplLibs }

    List<DopplDependency> getTranslateDopplTestLibs() { return _buildContext.getDependencyResolver().translateDopplTestLibs }

    private FileCollection replaceOverlayFilterJava(List<FileTree> sourceDirs) {

        FileTree allFiles = new UnionFileTree("asdf", (Collection<? extends FileTree>)sourceDirs)

        DopplConfig dopplConfig = DopplConfig.from(project)

        allFiles = allFiles.matching(new PatternSet().include("**/*.java"))

        List<String> overlaySourceDirs = dopplConfig.overlaySourceDirs
        Set<String> overlayClasses = new HashSet<>()
        List<FileTree> overlayTrees = new ArrayList<>()

        for (String sd  : overlaySourceDirs) {
            ConfigurableFileTree fileTree = project.fileTree(dir: sd, includes: ["**/*.java"])
            FileTree matchingFileTree = fileTree.matching(dopplConfig.translatePattern)

            for (File overlayFile : matchingFileTree.files) {
                overlayClasses.add(overlayFile.getPath().substring(fileTree.getDir().getPath().length()))
            }

            overlayTrees.add(matchingFileTree)
        }

        Set<File> allFilesFiles = allFiles.files
        Set<File> toRemove = new HashSet<>()

        //Yeah, this is not great. TODO: better matching algo
        for (File af : allFilesFiles) {

            String sourceFilePath = af.getPath()
            for (String overlayClassFilenameSuffix : overlayClasses) {
                if(sourceFilePath.endsWith(overlayClassFilenameSuffix))
                {
                    toRemove.add(af)
                }
            }
        }

        for (FileTree tree : overlayTrees) {
            allFiles.add(tree)
        }

        FileCollection resultCollection = allFiles;

        if (dopplConfig.translatePattern != null) {
            resultCollection = resultCollection.matching(dopplConfig.translatePattern)
        }

        resultCollection = resultCollection.filter {f ->
            !toRemove.contains(f)
        }

        return Utils.mapSourceFiles(project, resultCollection, getTranslateSourceMapping())
    }

    @TaskAction
    void translate(IncrementalTaskInputs inputs) {

        DopplConfig dopplConfig = DopplConfig.from(project)

        List<String> translateArgs = getTranslateArgs()

        // Don't evaluate this expensive property multiple times.
        FileCollection originalMainSrcFiles = getMainSrcFiles()
        FileCollection originalTestSrcFiles = getTestSrcFiles()

        FileCollection mainSrcFilesChanged, testSrcFilesChanged

        boolean forceFullBuild = !inputs.incremental
        mainSrcFilesChanged = project.files()
        testSrcFilesChanged = project.files()

        inputs.outOfDate(new Action<InputFileDetails>() {
            @Override
            void execute(InputFileDetails details) {

                if(forceFullBuild)
                    return

                // We must filter by srcFiles, since all possible input files are @InputFiles to this task.
                if (originalMainSrcFiles.contains(details.file)) {
                    mainSrcFilesChanged += project.files(details.file)
                } else if (originalTestSrcFiles.contains(details.file)) {
                    testSrcFilesChanged += project.files(details.file)
                } else {
                    forceFullBuild = true
                }
            }
        })

        inputs.removed(new Action<InputFileDetails>() {
            @Override
            void execute(InputFileDetails details) {
                forceFullBuild = true
            }
        })

        println("forceFullBuild: "+ forceFullBuild + "/inputs.incremental: "+ inputs.incremental)

        if (forceFullBuild) {
            // A change outside of the source set directories has occurred, so an incremental build isn't possible.
            // The most common such change is in the JAR for a dependent library, for example if Java project
            // that this project depends on had its source changed and was recompiled.
            Utils.projectClearDir(project, srcGenMainDir)
            Utils.projectClearDir(project, srcGenTestDir)
            mainSrcFilesChanged = originalMainSrcFiles
            testSrcFilesChanged = originalTestSrcFiles
        }

        List<File> translateSourceDirs = new ArrayList<>(getMainSrcDirs());

        def prefixMap = getPrefixes()

        doTranslate(
                project.files(translateSourceDirs.toArray()),
                srcMainObjcDir,
                srcGenMainDir,
                translateArgs,
                prefixMap,
                mainSrcFilesChanged,
                "mainSrcFilesArgFile",
                false,
                dopplConfig.emitLineDirectives
        )

        Utils.projectCopy(project, {
            from originalMainSrcFiles
            into srcGenMainDir
            setIncludeEmptyDirs(false)
            include '**/*.mappings'
        })

        if (prefixMap.size() > 0) {
            def prefixes = new File(srcGenMainDir, "prefixes.properties")
            def writer = new FileWriter(prefixes)

            Utils.propsFromStringMap(prefixMap).store(writer, null);

            writer.close()
        }

        // Translate test code. Tests are never built with --build-closure; otherwise
        // we will get duplicate symbol errors.
        // There is an edge-case that will fail: if the main and test code depend on
        // some other library X AND use --build-closure to translate X AND the API of X
        // needed by the test code is not a subset of the API of X used by the main
        // code, compilation will fail. The solution is to just build the whole library
        // X as a separate j2objc project and depend on it.
        //KPG: We don't pass '--build-closure' to j2objc. Evaluate if we want to add support.
        List<String> testTranslateArgs = new ArrayList<>(translateArgs)
        testTranslateArgs.removeAll('--build-closure')
        //*************** ^^^ THIS PART NEEDS TO GO AND/OR BE UPDATED. DOES NOTHING NOW!!!!!!

        doTranslate(
                project.files(getTestSrcDirs().toArray()),
                srcTestObjcDir,
                srcGenTestDir,
                testTranslateArgs,
                prefixMap,
                testSrcFilesChanged,
                "testSrcFilesArgFile",
                true,
                dopplConfig.emitLineDirectives
        )

        Utils.projectCopy(project, {
            from Utils.srcDirs(project, 'test', 'java')
            into srcGenTestDir
            include '**/*.mappings'
        })
    }

    void recursiveGrab(File dir, List<File> files)
    {
        if(dir.isDirectory())
        {
            File[] dirFiles = dir.listFiles()
            for (File f : dirFiles) {
                if(f.isDirectory())
                    recursiveGrab(f, files)
                else if(f.getName().endsWith(".java"))
                    files.add(f);
            }
        }
    }

    void doTranslate(
            FileCollection sourcepathDirs,
            File nativeSourceDir,
            File srcDir,
            List<String> translateArgs,
            Map<String, String> prefixMap,
            FileCollection srcFilesToTranslate,
            String srcFilesArgFilename,
            boolean testTranslate,
            boolean emitLineDirectives) {

        if (nativeSourceDir != null && nativeSourceDir.exists()) {
            Utils.projectCopy(project, {
                includeEmptyDirs = false
                from nativeSourceDir
                into srcDir
            })
        }

        Set<File> files = srcFilesToTranslate.getFiles()
        int num = files.size()
        logger.info("Translating $num files with j2objc...")
        if (files.size() == 0) {
            logger.info("No files to translate; skipping j2objc execution")
            return
        }

        String j2objcExecutable = "${getJ2objcHome()}/j2objc"

        String sourcepathArg = Utils.joinedPathArg(sourcepathDirs)

        List<DopplDependency> dopplLibs = new ArrayList<>(getTranslateDopplLibs())
        if (testTranslate) {
            dopplLibs.addAll(getTranslateDopplTestLibs())
        }
        def libs = Utils.dopplJarLibs(dopplLibs)

        //Classpath arg for translation. Includes user specified jars, j2objc 'standard' jars, and doppl dependency libs
        UnionFileCollection classpathFiles = new UnionFileCollection([
                project.files(getTranslateClasspaths()),
                project.files(Utils.j2objcLibs(getJ2objcHome(), getTranslateJ2objcLibs())),
                project.files(libs)
        ])

        // TODO: comment explaining ${project.buildDir}/classes
        String classpathArg = Utils.joinedPathArg(classpathFiles) +
                              Utils.pathSeparator() + "${project.buildDir}/classes"

        // Source files arguments
        List<String> srcFilesArgs = []
        int srcFilesArgsCharCount = 0
        for (File file in srcFilesToTranslate) {
            String filePath = file.getPath()
            srcFilesArgs.add(filePath)
            srcFilesArgsCharCount += filePath.length() + 1
        }

        // Handle command line that's too long
        // Allow up to 2,000 characters for command line excluding src files
        // http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html#commandlineargfile
        if (srcFilesArgsCharCount + 2000 > Utils.maxArgs()) {
            File srcFilesArgFile = new File(getTemporaryDir(), srcFilesArgFilename);
            FileWriter writer = new FileWriter(srcFilesArgFile);
            writer.append(srcFilesArgs.join('\n'));
            writer.close()
            // Replace src file arguments by referencing file
            srcFilesArgs = ["@${srcFilesArgFile.path}".toString()]
        }

        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        ByteArrayOutputStream stderr = new ByteArrayOutputStream()

        List<String> mappingFiles = new ArrayList<>()

        String path = mappingsInputPath()
        if (path != null) {
            mappingFiles.add(path);
        }

        Map<String, String> allPrefixes = new HashMap<>(prefixMap)

        for (DopplDependency lib : dopplLibs) {

            String mappingPath = Utils.findDopplLibraryMappings(lib.dependencyFolderLocation())
            if (mappingPath != null && !mappingPath.isEmpty()) {
                mappingFiles.add(mappingPath)
            }

            Properties prefixPropertiesFromFile = Utils.findDopplLibraryPrefixes(lib.dependencyFolderLocation())
            if (prefixPropertiesFromFile != null) {
                for (String name : prefixPropertiesFromFile.propertyNames()) {
                    allPrefixes.put(name, (String) properties.get(name))
                }
            }
        }

        try {
            Utils.projectExec(project, stdout, stderr, null, {
                executable j2objcExecutable

                // Arguments
                args "-d", srcDir
                if(emitLineDirectives)
                {
                    args "-g", ''
                }
//                args "--strip-reflection", ''
                args "-Xuse-javac", ''
                args "--package-prefixed-filenames", ''
                if (!testTranslate) {
                    args "--output-header-mapping", new File(srcDir, project.name + ".mappings").absolutePath
                }
                if (getIgnoreWeakAnnotations()) {
                    args "--ignore-weak-annotation", ''
                }
                if (mappingFiles.size() > 0) {
                    args "--header-mapping", mappingFiles.join(",")
                }
                args "-sourcepath", sourcepathArg
                args "-classpath", classpathArg
                translateArgs.each { String translateArg ->
                    args translateArg
                }

                allPrefixes.keySet().each { String packageString ->
                    args "--prefix", packageString + "=" + allPrefixes.get(packageString)
                }

                // File Inputs
                srcFilesArgs.each { String arg ->
                    // Can be list of src files or a single @/../srcFilesArgFile reference
                    args arg
                }

                setStandardOutput stdout
                setErrorOutput stderr
            })

        } catch (Exception exception) {  // NOSONAR
            // TODO: match on common failures and provide useful help
            throw exception
        }
    }
}
