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

package org.j2objcgradle.gradle.helper

import org.j2objcgradle.gradle.tasks.Utils
import net.lingala.zip4j.exception.ZipException
import org.gradle.api.Project
import org.jetbrains.annotations.NotNull

class J2objcRuntimeHelper {
    @NotNull
    static File runtimeDir(String version) {
        File j2objc = j2objcRuntimeDir()
        String name = ""
        if (version.startsWith("http")) {
            name = "j2objc"
        } else {
            name = githubDownload(version) ? "j2objc-$version" : version
        }
        return new File(j2objc, name)
    }

    @NotNull
    private static File j2objcRuntimeDir() {
        File home = new File(System.getProperty("user.home"))

        File j2objcDir = new File(home, ".j2objc")
        return new File(j2objcDir, "runtime")
    }

    static void cleanRuntimeDir(Project project){
        project.delete(j2objcRuntimeDir())
    }

    static File checkAndDownload(Project project, String version) throws IOException {
        File j2objcRuntimeDirFile = runtimeDir(version)
        if (checkJ2objcFoder(j2objcRuntimeDirFile))
            return j2objcRuntimeDirFile

        if(j2objcRuntimeDirFile.exists()){
            j2objcRuntimeDirFile.deleteDir()
        }

        downloadInstallJ2objcRuntime(project, version)

        return j2objcRuntimeDirFile
    }

    static def downloadInstallJ2objcRuntime(Project project, String version) {

        File runtimeDirFile = null

        if (version.startsWith("http")) {
            println "is custom http(s) url"
            runtimeDirFile = j2objcRuntimeDir()
        } else if(githubDownload(version)){
            println "is github"
            runtimeDirFile = j2objcRuntimeDir()
        }
        else{
            println "is not github"
            runtimeDirFile = runtimeDir(version)
            runtimeDirFile.mkdirs()
        }

        URL website = new URL(downloadUrl(version))
        URLConnection urlConnection = website.openConnection()
        File tempFile = new File(runtimeDirFile, Long.toString(System.currentTimeMillis()))

        println "tempFile ${tempFile.path}"

        try {
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            int contentLength = urlConnection.getContentLength()

            OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile))

            byte[] buf = new byte[65536]
            int read
            int totalRead = 0
            int totalParts = 60

            int partSize = contentLength / totalParts

            int nextPartLimit = partSize
            int partCount = 0

            int megs = contentLength / (1024 * 1024)
            System.out.println("Downloading J2objc " + version + " size " + megs + "m")
            while ((read = inputStream.read(buf)) > -1) {
                fos.write(buf, 0, read)
                totalRead += read

                if (totalRead >= nextPartLimit) {
                    partCount++
                    nextPartLimit += partSize
                    StringBuilder sb = new StringBuilder()
                    int i = 0
                    for (; i < partCount; i++) {
                        sb.append('.')
                    }
                    for (; i < totalParts; i++) {
                        sb.append(' ')
                    }
                    int downMegs = totalRead / (1024 * 1024)
                    System.out.println("[" + sb.toString() + "] " + downMegs + "/" + megs + "m")
                }
            }

            fos.close()

            System.out.println("Download complete")

            if (tempFile.length() != contentLength) {
                throw new IOException("File download failed (incorrect length)")
            }

            try {
                ByteArrayOutputStream stdout = new ByteArrayOutputStream()
                ByteArrayOutputStream stderr = new ByteArrayOutputStream()

                Utils.projectExec(project, stdout, stderr, null, {
                    executable "unzip"
                    args "$tempFile.name"
                    setStandardOutput stdout
                    setErrorOutput stderr
                    setWorkingDir runtimeDirFile
                })
            } catch (ZipException e) {
                throw new IOException(e)
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            if (urlConnection instanceof HttpURLConnection) {
                ((HttpURLConnection) urlConnection).disconnect()
            }
        }
    }
/**
     * Sanity check to see if the folder has j2objc
     *
     * @param runtimeDirFile
     */
    private static boolean checkJ2objcFoder(File runtimeDirFile) {
        return runtimeDirFile.exists() && new File(runtimeDirFile, "j2objc").exists()
    }

    /**
     * We're grabbing j2objc 2.1+ directly from github release urls. According to docs, there are no limits
     * to those downloads, so we shouldn't be causing a hassle to anybody :)
     *
     * https://help.github.com/articles/distributing-large-binaries/
     *
     * @param version
     * @return
     */
    private static String downloadUrl(String version) {
        //https://github.com/google/j2objc/releases/download/2.1.1/j2objc-2.1.1.zip

        if (version.startsWith("http")) {
            return version; // "Version" string contains runtime URL
        }

        if(githubDownload(version)){
            return "https://github.com/google/j2objc/releases/download/${version}/j2objc-${version}.zip"
        }else{
            return "https://s3.amazonaws.com/dopplmaven/j2objc_emul_" + version + ".zip"
        }
    }

    private static boolean githubDownload(String version){
        return !version.startsWith("2.0.6")
    }
}
