package com.equationl.hugo_gallery_uploader.util

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.Locale

// from https://stackoverflow.com/questions/11113974/what-is-the-cross-platform-way-of-obtaining-the-path-to-the-local-application-da

/**
 * Responsible for determining the directory to write application data, across
 * multiple platforms. See also:
 *
 *
 *  *
 * [
 * Linux: XDG Base Directory Specification
](https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html) *
 *
 *  *
 * [
 * Windows: Recognized environment variables
](https://learn.microsoft.com/en-us/windows/deployment/usmt/usmt-recognized-environment-variables) *
 *
 *  *
 * [
 * MacOS: File System Programming Guide
](https://developer.apple.com/library/archive/documentation/FileManagement/Conceptual/FileSystemProgrammingGuide/FileSystemOverview/FileSystemOverview.html) *
 *
 *
 *
 */
object UserDataDir {
    private val UNDEFINED: Path = Path.of("/")

    private val PROP_USER_HOME: String = System.getProperty("user.home")
    private val PROP_OS_VERSION: String = System.getProperty("os.version")
    private val ENV_APPDATA: String? = System.getenv("AppData")
    private val ENV_XDG_DATA_HOME: String? = System.getenv("XDG_DATA_HOME")

    @Throws(FileNotFoundException::class)
    fun getAppPath(appName: String = "HugoGalleryUpload"): Path {
        val osPath: Path = if (isWindows)
            winAppPath
        else
            if (isMacOs)
                macAppPath
            else
                if (isUnix)
                    unixAppPath
                else
                    UNDEFINED

        val path: Path = if (osPath == UNDEFINED)
            getDefaultAppPath(appName)
        else
            osPath.resolve(appName)

        return if (ensureExists(path)) path else fail(path)
    }

    @Throws(FileNotFoundException::class)
    private fun fail(path: Path): Path {
        throw FileNotFoundException(path.toString())
    }

    private val winAppPath: Path
        get() = if (ENV_APPDATA == null || ENV_APPDATA.isBlank())
            home(*winVerAppPath)
        else
            Path.of(ENV_APPDATA)

    private val winVerAppPath: Array<String>
        /**
         * Gets the application path with respect to the Windows version.
         *
         * @return The directory name paths relative to the user's home directory.
         */
        get() {
            return if (PROP_OS_VERSION.startsWith("5."))
                arrayOf("Application Data")
            else
                arrayOf("AppData", "Roaming")
        }

    private val macAppPath: Path
        get() {
            val path: Path = home("Library", "Application Support")

            return if (ensureExists(path)) path else UNDEFINED
        }

    private val unixAppPath: Path
        get() {
            // Fallback in case the XDG data directory is undefined.
            var path: Path = home(".local", "share")

            if (ENV_XDG_DATA_HOME != null && !ENV_XDG_DATA_HOME.isBlank()) {
                val xdgPath: Path = Path.of(ENV_XDG_DATA_HOME)

                path = if (ensureExists(xdgPath)) xdgPath else path
            }

            return path
        }

    /**
     * Returns a hidden directory relative to the user's home directory.
     *
     * @param appName The application name.
     * @return A suitable directory for storing application files.
     */
    private fun getDefaultAppPath(appName: String): Path {
        return home(".$appName")
    }

    private fun home(vararg paths: String): Path {
        return Path.of(PROP_USER_HOME, *paths)
    }

    /**
     * Verifies whether the path exists or was created.
     *
     * @param path The directory to verify.
     * @return `true` if the path already exists or was created,
     * `false` if the directory doesn't exist and couldn't be created.
     */
    private fun ensureExists(path: Path): Boolean {
        val file: File = path.toFile()
        return file.exists() || file.mkdirs()
    }

    private val isWindows: Boolean
        get() {
            val os = (System.getProperty("os.name")).uppercase(Locale.getDefault())
            println("os == $os")
            return os.contains("WIN")
        }

    private val isMacOs: Boolean
        get() {
            val os = (System.getProperty("os.name")).uppercase(Locale.getDefault())
            println("os == $os")
            return os.contains("MAC")
        }

    private val isUnix: Boolean
        get() {
            val os = (System.getProperty("os.name")).uppercase(Locale.getDefault())
            println("os == $os")
            return os.contains("LINUX")
        }
}