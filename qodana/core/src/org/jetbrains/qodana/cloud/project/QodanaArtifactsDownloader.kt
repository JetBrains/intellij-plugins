package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.openapi.util.io.FileUtil
import com.intellij.platform.util.progress.indeterminateStep
import com.intellij.util.download.DownloadableFileDescription
import com.intellij.util.download.DownloadableFileService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.registry.QodanaRegistry
import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

/*
 * Provides a capability to download artifact using supported schemes to the target folder.
 * The platform API behaviour doesn't match our testing needs. When passed local file,
 * it doesn't copy the file to the destination folder, thus breaking our logic for the artifacts.
 */
interface QodanaArtifactsDownloader {
  companion object {
    fun getInstance(): QodanaArtifactsDownloader = service()
  }

  suspend fun download(url: String, filename: String, presentableUrl: String, presentableFilename: String, pathToDownload: Path? = null): File?
}

class QodanaArtifactsDownloaderImpl: QodanaArtifactsDownloader {

  internal suspend fun downloadAttempt(
    url: String,
    filename: String,
    presentableUrl: String,
    presentableFilename: String,
    pathToDownload: Path?
  ): File? {
    val service = DownloadableFileService.getInstance()

    val filePathToDownloadTo = pathToDownload?.toFile()?.resolve(filename)
    withContext(QodanaDispatchers.IO) {
      try {
        if (filePathToDownloadTo?.exists() == true) {
          filePathToDownloadTo.delete()
        }
      }
      catch (_ : IOException) { // ignore
      }
    }
    val fileToDownload = filePathToDownloadTo ?: withContext(QodanaDispatchers.IO) {
      FileUtil.createTempFile(filename, null, true)
    }

    val fileDescription = object : DownloadableFileDescription by service.createFileDescription(url, fileToDownload.name) {
      override fun getPresentableDownloadUrl(): String = presentableUrl

      override fun getPresentableFileName(): String = presentableFilename
    }

    val downloader = service.createDownloader(listOf(fileDescription), presentableUrl)

    return withContext(QodanaDispatchers.IO) {
      indeterminateStep {
        coroutineToIndicator {
          downloader.download(fileToDownload.parentFile).firstOrNull()?.first
        }
      }
    }
  }

  override suspend fun download(
    url: String,
    filename: String,
    presentableUrl: String,
    presentableFilename: String,
    pathToDownload: Path?
  ): File? {
    return flow {
      val downloadedFile = downloadAttempt(url, filename, presentableUrl, presentableFilename, pathToDownload)
      emit(downloadedFile)
    }.retryWhen { e, attempt ->
      if (e is IOException && attempt < QodanaRegistry.cloudDownloadRetriesCount) {
        indeterminateStep(QodanaBundle.message("progress.title.qodana.retry.loading.report")) {
          delay((3.5).pow(attempt.toInt()).seconds)
        }
        true
      } else {
        false
      }
    }.first()
  }
}

class QodanaArtifactsDownloaderTestImpl: QodanaArtifactsDownloader {
  override suspend fun download(
    url: String,
    filename: String,
    presentableUrl: String,
    presentableFilename: String,
    pathToDownload: Path?
  ): File? {
    val file = QodanaArtifactsDownloaderImpl().downloadAttempt(url, filename, presentableUrl, presentableFilename, pathToDownload)
    if (file != null && pathToDownload != null) {
      return file.copyTo(pathToDownload.resolve(presentableFilename).toFile(), true)
    }
    return file
  }
}