package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.markdown.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.plugins.markdown.extensions.MarkdownCodeFencePluginGeneratingProvider;
import org.intellij.plugins.markdown.extensions.plantuml.PlantUMLPluginGeneratingProvider;
import org.intellij.plugins.markdown.lang.generating.MarkdownHtmlGeneratingDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MarkdownCodeFencePluginsGeneratingDescriptor implements MarkdownHtmlGeneratingDescriptor {
  @Override
  public Map<IElementType, GeneratingProvider> customizeHtmlGeneratingProviders(@NotNull MarkdownCodeFencePluginCacheProvider pluginCacheProvider) {
    return ContainerUtil.newHashMap(Pair.create(MarkdownElementTypes.CODE_FENCE,
                                                new MarkdownCodeFenceGeneratingProvider(
                                                  new MarkdownCodeFencePluginGeneratingProvider[]{
                                                    new PlantUMLPluginGeneratingProvider(pluginCacheProvider)})));
  }
}