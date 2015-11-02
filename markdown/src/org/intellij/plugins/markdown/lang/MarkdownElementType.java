/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.plugins.markdown.lang;

import com.intellij.psi.tree.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.flavours.gfm.GFMTokenTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MarkdownElementType extends IElementType {

  @NotNull
  private static final Map<org.intellij.markdown.IElementType, IElementType> markdownToPlatformTypeMap =
    new HashMap<org.intellij.markdown.IElementType, IElementType>();
  @NotNull
  private static final Map<IElementType, org.intellij.markdown.IElementType> platformToMarkdownTypeMap =
    new HashMap<IElementType, org.intellij.markdown.IElementType>();

  public MarkdownElementType(@NotNull @NonNls String debugName) {
    super(debugName, MarkdownLanguage.INSTANCE);
  }

  @Override
  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    return MessageFormat.format("Markdown:{0}", super.toString());
  }

  @Contract("!null -> !null")
  public synchronized static IElementType platformType(@Nullable org.intellij.markdown.IElementType markdownType) {
    if (markdownType == null) {
      return null;
    }

    if (markdownToPlatformTypeMap.containsKey(markdownType)) {
      return markdownToPlatformTypeMap.get(markdownType);
    }

    final IElementType result;
    if (markdownType == MarkdownElementTypes.PARAGRAPH
        || markdownType == MarkdownTokenTypes.ATX_CONTENT
        || markdownType == MarkdownTokenTypes.SETEXT_CONTENT
        || markdownType == GFMTokenTypes.CELL) {
      result = new MarkdownLazyElementType(markdownType.toString());
    }
    else {
      result = new MarkdownElementType(markdownType.toString());
    }
    markdownToPlatformTypeMap.put(markdownType, result);
    platformToMarkdownTypeMap.put(result, markdownType);
    return result;
  }

  @Contract("!null -> !null")
  public synchronized static org.intellij.markdown.IElementType markdownType(@Nullable IElementType platformType) {
    if (platformType == null) {
      return null;
    }
    return platformToMarkdownTypeMap.get(platformType);
  }
}
