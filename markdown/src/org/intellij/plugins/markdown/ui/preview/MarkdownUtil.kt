package org.intellij.plugins.markdown.ui.preview

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.ContainerUtil
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager
import org.jetbrains.annotations.NonNls
import java.io.File
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object MarkdownUtil {
  private val LOG = Logger.getInstance(MarkdownUtil::class.java)

  fun md5(buffer: String, @NonNls key: String): String {
    var md5: MessageDigest? = null
    try {
      md5 = MessageDigest.getInstance("MD5")
    }
    catch (e: NoSuchAlgorithmException) {
      LOG.error("Cannot find 'md5' algorithm; ", e)
    }

    Objects.requireNonNull<MessageDigest>(md5).update(buffer.toByteArray(StandardCharsets.UTF_8))
    val code = md5!!.digest(key.toByteArray(StandardCharsets.UTF_8))
    val bi = BigInteger(code).abs()
    return bi.abs().toString(16)
  }

  fun generateMarkdownHtml(file: VirtualFile, text: String, project: Project?): String {
    val parent = file.parent
    val baseUri = if (parent != null) File(parent.path).toURI() else null

    val parsedTree = MarkdownParser(MarkdownParserManager.FLAVOUR).buildMarkdownTreeFromString(text)
    val cacheCollector = MarkdownCodeFencePluginCacheCollector(file)

    val linkMap = LinkMap.buildLinkMap(parsedTree, text)
    val map = ContainerUtil.newHashMap(MarkdownParserManager.FLAVOUR.createHtmlGeneratingProviders(linkMap, baseUri))
    map.putAll(MarkdownParserManager.CODE_FENCE_PLUGIN_FLAVOUR.createHtmlGeneratingProviders(cacheCollector))
    if (project != null) {
      map[MarkdownElementTypes.IMAGE] = IntelliJImageGeneratingProvider(linkMap, baseUri, project)
    }

    val html = HtmlGenerator(text, parsedTree, map, true).generateHtml()

    MarkdownCodeFencePluginCache.getInstance().registerCacheProvider(cacheCollector)

    return html
  }
}