package org.intellij.plugins.markdown.lang.generating;

import com.intellij.openapi.extensions.ExtensionPointName;
import org.intellij.markdown.IElementType;
import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.plugins.markdown.extensions.MarkdownCodeFencePluginGeneratingProvider;
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCacheProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface MarkdownHtmlGeneratingDescriptor {
  ExtensionPointName<MarkdownHtmlGeneratingDescriptor> EP_NAME = ExtensionPointName.create("org.intellij.markdown.htmlGeneratingProvider");

  Map<IElementType, GeneratingProvider> customizeHtmlGeneratingProviders(@NotNull MarkdownCodeFencePluginCacheProvider codeFenceCacheVisitor);

  MarkdownCodeFencePluginGeneratingProvider[] getCodeFencePluginProviders();
}