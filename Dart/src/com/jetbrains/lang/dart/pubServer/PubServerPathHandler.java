package com.jetbrains.lang.dart.pubServer

import com.google.common.net.UrlEscapers
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.isRegularBrowser
import com.intellij.util.io.origin
import com.intellij.util.io.referrer
import com.intellij.util.io.userAgent
import com.jetbrains.lang.dart.sdk.DartSdk
import com.jetbrains.lang.dart.util.DartUrlResolver
import com.jetbrains.lang.dart.util.PubspecYamlUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.EmptyHttpHeaders
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaders
import org.jetbrains.builtInWebServer.*

private val LOG = logger<PubServerPathHandler>()

class PubServerPathHandler : WebServerPathHandlerAdapter() {
  override fun process(path: String, project: Project, request: FullHttpRequest, context: ChannelHandlerContext): Boolean {
    val sdk = DartSdk.getDartSdk(project)
    if (sdk == null || StringUtil.compareVersionNumbers(sdk.version, "1.6") < 0) {
      return false
    }

    val servedDirAndPathForPubServer = getServedDirAndPathForPubServer(project, path) ?: return false
    val isSignedRequest = request.isSignedRequest()
    var validateResult: HttpHeaders? = null
    val userAgent = (request).userAgent
    if (!isSignedRequest &&
        userAgent != null &&
        request.isRegularBrowser() &&
        request.origin == null &&
        request.referrer == null &&
        (request.uri().endsWith(".map") || request.uri().endsWith(".dart"))) {
      val matcher = chromeVersionFromUserAgent.matcher(userAgent)
      if (matcher.find() && StringUtil.compareVersionNumbers(matcher.group(1), "51") >= 0) {
        validateResult = EmptyHttpHeaders.INSTANCE
      }
    }

    if (validateResult == null) {
      validateResult = validateToken(request, context.channel(), isSignedRequest)
    }

    if (validateResult != null) {
      PubServerManager.getInstance(project).send(context.channel(), request, validateResult, servedDirAndPathForPubServer.first,
                                                 servedDirAndPathForPubServer.second)
    }
    return true
  }
}

private fun getServedDirAndPathForPubServer(project: Project, path: String): Pair<VirtualFile, String>? {
  // File with requested path may not exist, pub server will generate and serve it.
  // Here we find deepest (if nested) Dart project (aka Dart package) folder and its existing subfolder that can be served by pub server.

  // There may be 2 content roots with web/foo.html and web/bar.html files in them correspondingly. We need to catch the correct 'web' folder.
  // First see if full path can be resolved to a file
  val file = WebServerPathToFileManager.getInstance(project).findVirtualFile(path)
  if (file != null) {
    val pubspec = PubspecYamlUtil.findPubspecYamlFile(project, file) ?: return null
    val dartRoot = pubspec.parent
    val relativePath = FileUtil.getRelativePath(dartRoot.path, file.path, '/')
    // we only handle files 2 levels deeper than the Dart project root
    val slashIndex = relativePath?.indexOf('/') ?: -1
    val folderName = if (slashIndex == -1) null else relativePath!!.substring(0, slashIndex)
    if (folderName == null || folderName == "build" || folderName == "lib" || DartUrlResolver.PACKAGES_FOLDER_NAME == folderName) {
      return null
    }

    val servedDir = dartRoot.findChild(folderName)
    val pubServePath = relativePath!!.substring(slashIndex)
    return Pair.create(servedDir, escapeUrl(pubServePath))
  }

  // If above failed then take the longest path part that corresponds to an existing folder
  var servedDir: VirtualFile? = null
  var pubServePath: String? = null

  var slashIndex = -1
  while (true) {
    slashIndex = path.indexOf('/', slashIndex + 1)
    if (slashIndex < 0) {
      break
    }
    
    val pathPart = path.substring(0, slashIndex)
    val dirInfo = WebServerPathToFileManager.getInstance(project).getPathInfo(pathPart)
    if (dirInfo == null || !dirInfo.isDirectory()) {
      continue
    }

    val dir = dirInfo.getOrResolveVirtualFile()
    val parentDir = dir?.parent
    if (parentDir != null && parentDir.findChild(PubspecYamlUtil.PUBSPEC_YAML) != null) {
      if ("build" == dirInfo.name ||
          "lib" == dirInfo.name ||
          DartUrlResolver.PACKAGES_FOLDER_NAME == dir!!.name) {
        return null // contents of "build" folder should be served by the IDE internal web server directly, i.e. without pub serve
      }

      servedDir = dir
      pubServePath = path.substring(slashIndex)
      // continue looking for nested Dart project
    }
  }

  return if (servedDir != null) Pair.create<VirtualFile, String>(servedDir, escapeUrl(pubServePath!!)) else null
}

private fun escapeUrl(path: String): String {
  try {
    // need to restore slash separators after UrlEscapers.urlPathSegmentEscaper work
    return StringUtil.replace(UrlEscapers.urlPathSegmentEscaper().escape(path), "%2F", "/")
  }
  catch (e: Exception) {
    LOG.warn(path, e)
    return path
  }

}