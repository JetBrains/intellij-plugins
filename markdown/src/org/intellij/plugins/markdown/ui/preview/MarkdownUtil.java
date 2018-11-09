package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.markdown.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

public class MarkdownUtil {
  private static final Logger LOG = Logger.getInstance(MarkdownUtil.class);

  public static String md5(String buffer, @NonNls String key) {
    MessageDigest md5 = null;
    try {
      md5 = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e) {
      LOG.error("Cannot find 'md5' algorithm; ", e);
    }

    Objects.requireNonNull(md5).update(buffer.getBytes(StandardCharsets.UTF_8));
    byte[] code = md5.digest(key.getBytes(StandardCharsets.UTF_8));
    BigInteger bi = new BigInteger(code).abs();
    return bi.abs().toString(16);
  }

  @NotNull
  public static String generateMarkdownHtml(@NotNull VirtualFile file, @NotNull String text, @Nullable Project project) {
    final VirtualFile parent = file.getParent();
    final URI baseUri = parent != null ? new File(parent.getPath()).toURI() : null;

    final ASTNode parsedTree = new MarkdownParser(MarkdownParserManager.FLAVOUR).buildMarkdownTreeFromString(text);
    MarkdownCodeFencePluginCacheCollector cacheCollector = new MarkdownCodeFencePluginCacheCollector(file);

    LinkMap linkMap = LinkMap.Builder.buildLinkMap(parsedTree, text);
    Map<IElementType, GeneratingProvider> map = ContainerUtil.newHashMap(MarkdownParserManager.FLAVOUR.createHtmlGeneratingProviders(linkMap, baseUri));
    map.putAll(MarkdownParserManager.CODE_FENCE_PLUGIN_FLAVOUR.createHtmlGeneratingProviders(cacheCollector));
    if (project != null) {
      map.put(MarkdownElementTypes.IMAGE, new IntelliJImageGeneratingProvider(linkMap, baseUri, project));
    }

    String html = new HtmlGenerator(text, parsedTree, map, true).generateHtml();

    MarkdownCodeFencePluginCache.getInstance().registerCacheProvider(cacheCollector);

    return html;
  }
}