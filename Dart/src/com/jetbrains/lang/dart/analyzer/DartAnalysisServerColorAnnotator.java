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
package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.generated.types.HighlightRegionType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService.PluginHighlightRegion;
import com.jetbrains.lang.dart.highlight.DartSyntaxHighlighterColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartAnalysisServerColorAnnotator extends ExternalAnnotator<Object, Object> {
  @Nullable
  private static String getHighlightType(String type) {
    if (type.equals(HighlightRegionType.ANNOTATION)) {
      return DartSyntaxHighlighterColors.DART_METADATA;
    }
    if (type.equals(HighlightRegionType.BUILT_IN)) {
      return DartSyntaxHighlighterColors.DART_KEYWORD;
    }
    if (type.equals(HighlightRegionType.CLASS)) {
      return DartSyntaxHighlighterColors.DART_CLASS;
    }
    if (type.equals(HighlightRegionType.CONSTRUCTOR)) {
      return DartSyntaxHighlighterColors.DART_CONSTRUCTOR_DECLARATION;
    }
    if (type.equals(HighlightRegionType.DYNAMIC_TYPE)) {
      return DartSyntaxHighlighterColors.DART_VARIABLE_OF_DYNAMIC_TYPE;
    }
    if (type.equals(HighlightRegionType.ENUM)) {
      return DartSyntaxHighlighterColors.DART_ENUM;
    }
    if (type.equals(HighlightRegionType.ENUM_CONSTANT)) {
      return DartSyntaxHighlighterColors.DART_ENUM_CONSTANT;
    }
    if (type.equals(HighlightRegionType.FIELD)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_VARIABLE_ACCESS;
    }
    if (type.equals(HighlightRegionType.FIELD_STATIC)) {
      return DartSyntaxHighlighterColors.DART_STATIC_MEMBER_VARIABLE_ACCESS;
    }
    if (type.equals(HighlightRegionType.FUNCTION)) {
      return DartSyntaxHighlighterColors.DART_FUNCTION;
    }
    if (type.equals(HighlightRegionType.FUNCTION_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_TOP_LEVEL_FUNCTION_DECLARATION;
    }
    if (type.equals(HighlightRegionType.FUNCTION_TYPE_ALIAS)) {
      return DartSyntaxHighlighterColors.DART_FUNCTION_TYPE_ALIAS;
    }
    if (type.equals(HighlightRegionType.GETTER_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_GETTER_DECLARATION;
    }
    if (type.equals(HighlightRegionType.LOCAL_VARIABLE)) {
      return DartSyntaxHighlighterColors.DART_LOCAL_VARIABLE_ACCESS;
    }
    if (type.equals(HighlightRegionType.IMPORT_PREFIX)) {
      return DartSyntaxHighlighterColors.DART_IMPORT_PREFIX;
    }
    if (type.equals(HighlightRegionType.KEYWORD)) {
      return DartSyntaxHighlighterColors.DART_KEYWORD;
    }
    if (type.equals(HighlightRegionType.LABEL)) {
      return DartSyntaxHighlighterColors.DART_LABEL;
    }
    if (type.equals(HighlightRegionType.LOCAL_VARIABLE_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_LOCAL_VARIABLE;
    }
    if (type.equals(HighlightRegionType.METHOD)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_FUNCTION_CALL;
    }
    if (type.equals(HighlightRegionType.METHOD_STATIC)) {
      return DartSyntaxHighlighterColors.DART_STATIC_MEMBER_FUNCTION_CALL;
    }
    if (type.equals(HighlightRegionType.METHOD_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_FUNCTION;
    }
    if (type.equals(HighlightRegionType.METHOD_DECLARATION_STATIC)) {
      return DartSyntaxHighlighterColors.DART_STATIC_MEMBER_FUNCTION;
    }
    if (type.equals(HighlightRegionType.PARAMETER)) {
      return DartSyntaxHighlighterColors.DART_PARAMETER;
    }
    if (type.equals(HighlightRegionType.SETTER_DECLARATION)) {
      return DartSyntaxHighlighterColors.DART_SETTER_DECLARATION;
    }
    if (type.equals(HighlightRegionType.TOP_LEVEL_VARIABLE)) {
      return DartSyntaxHighlighterColors.DART_TOP_LEVEL_VARIABLE_DECLARATION;
    }
    if (type.equals(HighlightRegionType.TYPE_NAME_DYNAMIC)) {
      return DartSyntaxHighlighterColors.DART_KEYWORD;
    }
    if (type.equals(HighlightRegionType.TYPE_PARAMETER)) {
      return DartSyntaxHighlighterColors.DART_TYPE_PARAMETER;
    }
    return null;
  }

  @Override
  public void apply(@NotNull PsiFile file, Object annotationResult, @NotNull AnnotationHolder holder) {
    final VirtualFile virtualFile = file.getVirtualFile();
    final List<PluginHighlightRegion> regions = DartAnalysisServerService.getInstance().getHighlight(virtualFile);
    for (PluginHighlightRegion region : regions) {
      final String type = region.getType();
      final String key = getHighlightType(type);
      if (key != null) {
        final TextAttributesKey textAttributes = TextAttributesKey.createTextAttributesKey(key);
        final TextRange textRange = new TextRange(region.getOffset(), region.getOffset() + region.getLength());
        holder.createInfoAnnotation(textRange, null).setTextAttributes(textAttributes);
      }
    }
  }

  @Nullable
  @Override
  public Object collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
    return new Object();
  }

  @Nullable
  @Override
  public Object doAnnotate(Object collectedInfo) {
    return new Object();
  }
}
