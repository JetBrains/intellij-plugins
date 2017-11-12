package org.intellij.plugins.markdown.extensions.plantuml;

import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.markdown.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor;
import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.plugins.markdown.extensions.MarkdownCodeFencePluginGeneratingProvider;
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFenceGeneratingProvider;
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCacheProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CodeFencePluginFlavourDescriptor extends CommonMarkFlavourDescriptor {
  @NotNull
  public Map<IElementType, GeneratingProvider> createHtmlGeneratingProviders(@NotNull MarkdownCodeFencePluginCacheProvider codeFencePluginCache) {
    return ContainerUtil.newHashMap(Pair.create(MarkdownElementTypes.CODE_FENCE,
                                                new MarkdownCodeFenceGeneratingProvider(
                                                  new MarkdownCodeFencePluginGeneratingProvider[]{
                                                    new PlantUMLPluginGeneratingProvider(codeFencePluginCache)})));
  }
}