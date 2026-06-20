// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}

tasks.register("downloadAndExtractRepo") {
  doLast {
    val urlConnection = java.net.URL("https://github.com/xscore-coder/SMC-Vodafone-V2/archive/refs/heads/main.zip").openConnection()
    urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0")
    val zipFile = file("build/repo.zip")
    zipFile.parentFile.mkdirs()
    println("Downloading zip...")
    urlConnection.getInputStream().use { input ->
      zipFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }
    println("Downloaded. Extracting...")
    java.util.zip.ZipInputStream(zipFile.inputStream()).use { zip ->
      var entry = zip.nextEntry
      while (entry != null) {
        val parts = entry.name.split("/", limit = 2)
        if (parts.size > 1 && parts[1].isNotEmpty()) {
          val relativePath = parts[1]
          val destFile = file(relativePath)
          if (entry.isDirectory) {
            destFile.mkdirs()
          } else {
            destFile.parentFile.mkdirs()
            destFile.outputStream().use { output ->
              zip.copyTo(output)
            }
          }
        }
        zip.closeEntry()
        entry = zip.nextEntry
      }
    }
    println("Extraction complete!")
  }
}

