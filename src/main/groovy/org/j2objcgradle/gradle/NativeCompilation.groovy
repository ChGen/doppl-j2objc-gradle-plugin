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
import org.j2objcgradle.gradle.tasks.TranslateTask
import org.j2objcgradle.gradle.tasks.Utils;

/**
 * Compilation of libraries for debug/release and architectures listed below.
 */
class NativeCompilation {

    static final String[] ALL_IOS_ARCHS = ['ios_arm64', 'ios_armv7', 'ios_armv7s', 'ios_i386', 'ios_x86_64']
    // TODO: Provide a mechanism to vary which OSX architectures are built.
    static final String[] ALL_OSX_ARCHS = ['x86_64']

    private final Project project

    NativeCompilation(Project project) {
        this.project = project
    }

    def getJ2objcPath() {
        return Utils.j2objcHome(project)
    }

    enum TargetSpec {
        TARGET_IOS_DEVICE,
        TARGET_IOS_SIMULATOR,
        TARGET_OSX,
    }

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
    void apply(Copy sourceGenerator, FrameworkConfig mainConfig, Copy testGenerator, FrameworkConfig testConfig) {

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
                        defineTarget(delegate, 'x86_64', TargetSpec.TARGET_OSX, 'x86_64')
                    }
                }
                platforms {
                    definePlatforms(delegate as NamedDomainObjectContainer<Platform>, ALL_OSX_ARCHS as List<String>)
                    definePlatforms(delegate as NamedDomainObjectContainer<Platform>, ALL_IOS_ARCHS as List<String>)
                }

                components {
                    j2objc(NativeLibrarySpec) {
                        sources {
                            objc {
                                source {
                                    // Note that contents of srcGenMainDir are generated by j2objcTranslate task.
                                    srcDir sourceGenerator
                                    srcDir "${buildDir}/j2objcHackToForceMainCompilation"
                                    include '**/*.m'
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
                        binaries {
                            all {
                                mainConfig.frameworks.each {
                                    linker.args "-framework", "$it"
                                }

                                mainConfig.libs.each {
                                    linker.args "-l$it"
                                }

                                mainConfig.onBinaryShouldBeConfigured(it)
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
                                testConfig.frameworks.each {
                                    linker.args "-framework", "$it"
                                }

                                testConfig.libs.each {
                                    linker.args "-l$it"
                                }

                                lib library: "j2objc", linkage: 'static'
                                objcCompiler.args "-I${sourceGenerator.destinationDir.path}/src"

                                linker.args "-lj2objc_main"

                                testConfig.onBinaryShouldBeConfigured(it)

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
                            cppCompiler.args '-std=c++11'

                            objcCompiler.args "-I$j2objcPath/include"
                            objcCompiler.args '-std=gnu11'
                            objcCompiler.args "-Wnon-literal-null-conversion"
                            objcCompiler.args "-fobjc-weak"
                            objcCompiler.args "-fmodules"
                            objcCompiler.args "-gmodules"

                            linker.args '-ObjC'

                            // J2ObjC provided libraries:
                            // TODO: should we link to all? Or just the 'standard' J2ObjC libraries?
                            linker.args '-ljre_emul'

                            // J2ObjC iOS library dependencies:
                            linker.args '-lc++'                    // C++ runtime for protobuf runtime
                            linker.args '-licucore'                // java.text

                            linker.args '-framework', 'Foundation' // core ObjC classes: NSObject, NSString
                            linker.args '-framework', 'Security'   // secure hash generation

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

        }
    }

}
