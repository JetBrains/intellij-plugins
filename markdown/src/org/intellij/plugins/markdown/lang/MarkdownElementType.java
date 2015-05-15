package org.intellij.plugins.markdown.lang;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MarkdownElementType extends IElementType {

  @NotNull
  private static final Map<org.intellij.markdown.IElementType, MarkdownElementType> markdownToPlatformTypeMap = new HashMap<org.intellij.markdown.IElementType, MarkdownElementType>();
  @NotNull
  private static final Map<IElementType, org.intellij.markdown.IElementType> platformToMarkdownTypeMap = new HashMap<IElementType, org.intellij.markdown.IElementType>();

  public MarkdownElementType(@NotNull @NonNls String debugName) {
    super(debugName, MarkdownLanguage.INSTANCE);
  }

  @Override
  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    return MessageFormat.format("Markdown:{0}", super.toString());
  }

  @Nullable
  public synchronized static IElementType platformType(@Nullable org.intellij.markdown.IElementType markdownType) {
    if (markdownType == null) {
      return null;
    }

    if (markdownToPlatformTypeMap.containsKey(markdownType)) {
      return markdownToPlatformTypeMap.get(markdownType);
    }
    final MarkdownElementType result = new MarkdownElementType(markdownType.toString());
    markdownToPlatformTypeMap.put(markdownType, result);
    platformToMarkdownTypeMap.put(result, markdownType);
    return result;
  }

  @Nullable
  public synchronized static org.intellij.markdown.IElementType markdownType(@Nullable IElementType platformType) {
    if (platformType == null) {
      return null;
    }
    return platformToMarkdownTypeMap.get(platformType);
  }
}
