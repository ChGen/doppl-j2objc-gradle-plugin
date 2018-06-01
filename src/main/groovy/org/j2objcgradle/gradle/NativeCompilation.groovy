package org.j2objcgradle.gradle;

import groovy.transform.PackageScope;
import org.gradle.api.*
import org.gradle.api.tasks.Copy;
import org.gradle.nativeplatform.NativeExecutableSpec;
import org.gradle.nativeplatform.NativeLibraryBinary;
import org.gradle.nativeplatform.NativeLibrarySpec;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.GccPlatformToolChain;
import org.gradle.platform.base.Platform
import org.j2objcgradle.gradle.tasks.TranslateDependenciesTask
import org.j2objcgradle.gradle.tasks.TranslateTask;

/**
 * Compilation of libraries for debug/release and architectures listed below.
 */
class NativeCompilation {

    static final String[] ALL_IOS_ARCHS = ['ios_arm64', 'ios_armv7', 'ios_armv7s', 'ios_i386', 'ios_x86_64']
    // TODO: Provide a mechanism to vary which OSX architectures are built.
    static final String[] ALL_OSX_ARCHS = ['x86_64']

    private final Project project

    String j2objcPath = "/Users/bbanyai/.j2objc/runtime/j2objc-2.1.1"

    NativeCompilation(Project project) {
        this.project = project
    }

    enum TargetSpec {
        TARGET_IOS_DEVICE,
        TARGET_IOS_SIMULATOR,
        TARGET_OSX,
    }

    String[] simulatorClangArgs = [
            '-isysroot',
            '/Applications/Xcode.app/Contents/Developer/Platforms/' +
            'iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk',
            ]
    String[] iphoneClangArgs = [
            '-isysroot',
            '/Applications/Xcode.app/Contents/Developer/Platforms/' +
            'iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk',
            ]

    void definePlatforms(NamedDomainObjectContainer<Platform> d, List<String> names) {
        names.each { String name ->
            d.create(name, {
                    architecture name
            })
        }
    }

    void defineTarget(Clang d, String name, TargetSpec targetSpec, final String architecture) {
        d.target(name, new Action<GccPlatformToolChain>() {
            @Override
            void execute(GccPlatformToolChain gccPlatformToolChain) {
                // Arguments common to the compiler and linker.
                String[] clangArgs = [
                '-arch',
                        architecture]
                // Arguments specific to the compiler.
                String[] compilerArgs = []
                // Arguments specific to the linker.
                String[] linkerArgs = []
                def config = [
                "minVersionIos" : "9.0"
                ]

                switch (targetSpec) {
//                    case TargetSpec.TARGET_IOS_DEVICE:
//                        clangArgs += iphoneClangArgs
//                        clangArgs += ["-miphoneos-version-min=${config.minVersionIos}"]
//                        linkerArgs += ["-L$j2objcPath/lib"]
//                        break
//                    case TargetSpec.TARGET_IOS_SIMULATOR:
//                        clangArgs += simulatorClangArgs
//                        clangArgs += ["-mios-simulator-version-min=${config.minVersionIos}"]
//                        linkerArgs += ["-L$j2objcPath/lib"]
//                        break
                    case TargetSpec.TARGET_OSX:
                        linkerArgs += ["-L$j2objcPath/lib/macosx"]
                        linkerArgs += ['-framework', 'ExceptionHandling']
                        break
                }
                compilerArgs += clangArgs
                linkerArgs += clangArgs
                gccPlatformToolChain.objcCompiler.withArguments { List<String> args ->
                    args.addAll(compilerArgs)
                }
                gccPlatformToolChain.objcppCompiler.withArguments { List<String> args ->
                    args.addAll(compilerArgs)
                }
                gccPlatformToolChain.linker.withArguments { List<String> args ->
                    args.addAll(linkerArgs)
                }
            }
        })
    }

    @PackageScope
    @SuppressWarnings("grvy:org.codenarc.rule.size.NestedBlockDepthRule")
    void apply(Copy sourceGenerator, Copy testGenerator) {

        project.with {
            // Wire up dependencies with tasks created dynamically by native plugin(s).
            tasks.whenTaskAdded { Task task ->
                // The Objective-C native plugin will add tasks of the form 'compile...Objc' for each
                // combination of buildType, platform, and component.  Note that components having only
                // one buildType or platform will NOT have the given buildType/platform in the task name, so
                // we have to use a very broad regular expression.
                // For example task 'compileDebugTestJ2objcExecutableTestJ2objcObjc' compiles the debug
                // buildType of the executable binary 'testJ2objc' from the 'testJ2objc' component.
                if ((task.name =~ /^compile.*Objc$/).matches()) {
                    task.dependsOn sourceGenerator
                    task.dependsOn testGenerator
                }

                // Only static libraries are needed, so disable shared libraries dynamically.
                // There is no way to do this within the native binary model.
                if ((task.name =~ /^.*SharedLibrary.*$/).matches()) {
                    task.enabled = false
                }
            }

            apply plugin: 'objective-c'
            apply plugin: 'cpp'

            // TODO: Figure out a better way to force compilation.
            // We create these files so that before the first j2objcTranslate execution is performed, at least
            // one file exists for each of the Objective-C sourceSets, at project evaluation time.
            // Otherwise the Objective-C plugin skips creation of the compile tasks altogether.
            file("${buildDir}/j2objcHackToForceMainCompilation").mkdirs()
            file("${buildDir}/j2objcHackToForceMainCompilation/Empty.m").createNewFile()
            file("${buildDir}/j2objcHackToForceTestCompilation").mkdirs()
            file("${buildDir}/j2objcHackToForceTestCompilation/EmptyTest.m").createNewFile()

            model {
                buildTypes {
                    debug
                    release
                }
                toolChains {
                    // Modify clang command line arguments since we need them to vary by target.
                    // https://docs.gradle.org/current/userguide/nativeBinaries.html#withArguments
                    clang(Clang) {
//                        defineTarget(delegate, 'ios_arm64', TargetSpec.TARGET_IOS_DEVICE, 'arm64')
//                        defineTarget(delegate, 'ios_armv7', TargetSpec.TARGET_IOS_DEVICE, 'armv7')
//                        defineTarget(delegate, 'ios_armv7s', TargetSpec.TARGET_IOS_DEVICE, 'armv7s')
//                        defineTarget(delegate, 'ios_i386', TargetSpec.TARGET_IOS_SIMULATOR, 'i386')
//                        defineTarget(delegate, 'ios_x86_64', TargetSpec.TARGET_IOS_SIMULATOR, 'x86_64')
                        defineTarget(delegate, 'x86_64', TargetSpec.TARGET_OSX, 'x86_64')
                    }
                }
                platforms {
                    definePlatforms(delegate as NamedDomainObjectContainer<Platform>, ALL_OSX_ARCHS as List<String>)
                    definePlatforms(delegate as NamedDomainObjectContainer<Platform>, ALL_IOS_ARCHS as List<String>)
                }

                components {
                    // Builds library, e.g. "libPROJECT-j2objc.a"
                    j2objc(NativeLibrarySpec) {
                        sources {
                            objc {
                                source {
                                    // Note that contents of srcGenMainDir are generated by j2objcTranslate task.
                                    srcDir sourceGenerator
                                    srcDir "${buildDir}/j2objcHackToForceMainCompilation"
//                                    srcDirs j2objcConfig.extraObjcSrcDirs
                                    include '**/*.m'
//                                        include '**/*.cpp'
                                }
                                // NOTE: Gradle has not yet implemented automatically archiving the
                                // exportedHeaders, this serves solely as a signifier for now.
                                exportedHeaders {
                                    srcDir sourceGenerator
                                    include '**/*.h'
                                }
                            }
                            cpp {
                                source {
                                    // Note that contents of srcGenMainDir are generated by j2objcTranslate task.
                                    srcDir sourceGenerator
                                    srcDir "${buildDir}/j2objcHackToForceMainCompilation"

                                    include '**/*.cpp'
                                }
                                exportedHeaders {
                                    srcDir sourceGenerator
                                    include '**/*.h'
                                }
                            }

                        }

                        targetPlatform 'x86_64'
                    }



                    // Create an executable binary from a library containing just the test source code linked to
                    // the production library built above.
                    testJ2objc(NativeExecutableSpec) {
                        sources {
                            objc {
                                source {
                                    srcDirs testGenerator
                                    srcDirs "${buildDir}/j2objcHackToForceTestCompilation"
                                    include '**/*.m'
                                }
                                exportedHeaders {
                                    srcDirs testGenerator
                                    include '**/*.h'
                                }
                            }
                        }
                        binaries {
                            all {
                                lib library: "j2objc", linkage: 'static'
                                objcCompiler.args "-I${sourceGenerator.destinationDir.path}/src"

                                linker.args "-lj2objc_main"

                            }
                        }
                        targetPlatform 'x86_64'
                    }
                }

                binaries {
                    all {
                        if (toolChain in Clang) {

                            // If you want to override the arguments passed to the compiler and linker,
                            // you must configure the binaries in your own build.gradle.
                            // See "Gradle User Guide: 54.11. Configuring the compiler, assembler and linker"
                            // https://docs.gradle.org/current/userguide/nativeBinaries.html#N16030
                            // TODO: Consider making this configuration easier using plugin extension.
                            // If we do that, however, we will become inconsistent with Gradle Objective-C building.
                            cppCompiler.args "-I$j2objcPath/include"
                            cppCompiler.args "-I${sourceGenerator.destinationDir.path}/src/cpphelp"
                            cppCompiler.args '-std=c++11'

                            objcCompiler.args "-I$j2objcPath/include"
                            objcCompiler.args '-std=gnu11'

                            objcCompiler.args '-I/Users/bbanyai/.j2objc/runtime/j2objc-2.1.1/include/guava/com/google/common/collect'
                            objcCompiler.args '-I/Users/bbanyai/.j2objc/runtime/j2objc-2.1.1/include/guava'
                            objcCompiler.args "-Wnon-literal-null-conversion"
//                            objcCompiler.args "-fobjc-arc"
                            objcCompiler.args "-fobjc-weak"
                            objcCompiler.args "-fmodules"
                            objcCompiler.args "-gmodules"

                            //objcCompiler.args j2objcConfig.extraObjcCompilerArgs

                            linker.args '-ObjC'

                            // J2ObjC provided libraries:
                            // TODO: should we link to all? Or just the 'standard' J2ObjC libraries?
                            linker.args '-ljre_emul'
//                            j2objcConfig.linkJ2objcLibs.each { String libArg ->
//                                linker.args "-l$libArg"
//                            }

                            // J2ObjC iOS library dependencies:
                            linker.args '-lc++'                    // C++ runtime for protobuf runtime
                            linker.args '-licucore'                // java.text
                            linker.args '-lz'                      // java.util.zip
                            linker.args '-framework', 'Foundation' // core ObjC classes: NSObject, NSString
                            linker.args '-framework', 'Security'   // secure hash generation
                            linker.args '-liconv'
                            linker.args '-lsqlite3'
//                            linker.args j2objcConfig.extraLinkerArgs

                            if (buildType == buildTypes.debug) {
                                // Full debugging information.
                                objcCompiler.args '-g'
                                objcCompiler.args '-DDEBUG=1'
                            } else {  // release
                                // Per https://raw.githubusercontent.com/llvm-mirror/clang/8eb384a97cfdc244a5ab81026677bcbaf8cf2ecf/docs/CommandGuide/clang.rst
                                // this is a moderate level of optimization with extra optimizations
                                // to reduce code size.  It's use in release builds was verified in Xcode 7,
                                // and we aim to match the behavior, per:
                                // https://developer.apple.com/library/ios/qa/qa1795/_index.html#//apple_ref/doc/uid/DTS40014195-CH1-COMPILER
                                objcCompiler.args '-Os'
                            }
                        }
                    }
                }
            }

            // We need to run clang with the arguments that j2objcc would usually pass.
//            binaries.all {
//                // Only want to modify the Objective-C toolchain, not the JDK one.
//
//            }

            // Marker tasks to build all Objective-C libraries.
            // See Gradle User Guide: 54.14.5. Building all possible variants
            // https://docs.gradle.org/current/userguide/nativeBinaries.html#N161B3
//            task('j2objcBuildObjcDebug').configure {
//                dependsOn binaries.withType(NativeLibraryBinary).matching { NativeLibraryBinary lib ->
//                    // Internal build type is lowercase 'debug'
//                    lib.buildable && lib.buildType.name == 'debug'
//                }
//            }
//            task('j2objcBuildObjcRelease').configure {
//                dependsOn binaries.withType(NativeLibraryBinary).matching { NativeLibraryBinary lib ->
//                    // Internal build type is lowercase 'release'
//                    lib.buildable && lib.buildType.name == 'release'
//                }
//            }

        }
    }

}
