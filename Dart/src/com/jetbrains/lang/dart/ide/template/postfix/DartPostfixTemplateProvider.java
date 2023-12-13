// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.template.postfix;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.PostfixTemplateDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class DartPostfixTemplateProvider implements PostfixTemplateProvider {
  private static final String UNINITIALIZED_KEY = "none";
  private static final Map<String, Set<PostfixTemplate>> TEMPLATE_CACHE = new HashMap<>();

  static {
    Set<PostfixTemplate> one = new HashSet<>();
    one.add(new DartRemotePostfixTemplate("none", ".none", "none"));
    // Prime the cache to enable postfix completion UI in Preferences before any analysis server is started.
    TEMPLATE_CACHE.put(UNINITIALIZED_KEY, one);
  }

  @Nullable
  public static Set<PostfixTemplate> getTemplates(String version) {
    return TEMPLATE_CACHE.get(version);
  }

  public static void initializeTemplates(@NotNull final DartAnalysisServerService service) {
    String version = service.getSdkVersion();
    if (TEMPLATE_CACHE.get(version) != null) return;

    PostfixTemplateDescriptor[] templates = service.edit_listPostfixCompletionTemplates();
    Set<PostfixTemplate> set = new HashSet<>();
    if (templates != null) {
      try {
        for (PostfixTemplateDescriptor template : templates) {
          set.add(DartRemotePostfixTemplate.createTemplate(template));
        }
        TEMPLATE_CACHE.put(version, set);
      }
      catch (Exception ex) {
        Logger.getInstance(DartPostfixTemplateProvider.class).error(ex);
      }
    }
  }

  @NotNull
  @Override
  public String getId() {
    return "builtin.dart";
  }

  @NotNull
  @Override
  public Set<PostfixTemplate> getTemplates() {
    // Find the largest initialized set. This may return templates that are not recognized
    // by the analysis server actually used for expansion, but at least the user will see
    // all possible templates.
    Set<PostfixTemplate> set = TEMPLATE_CACHE.get(UNINITIALIZED_KEY);
    for (Set<PostfixTemplate> cache : TEMPLATE_CACHE.values()) {
      if (set.size() < cache.size()) {
        set = cache;
      }
    }
    return set;
  }

  @Override
  public boolean isTerminalSymbol(char currentChar) {
    return currentChar == '.' || currentChar == '!';
  }

  @Override
  public void preExpand(@NotNull PsiFile file, @NotNull Editor editor) {
  }

  @Override
  public void afterExpand(@NotNull PsiFile file, @NotNull Editor editor) {
  }

  @NotNull
  @Override
  public PsiFile preCheck(@NotNull PsiFile copyFile, @NotNull Editor realEditor, int currentOffset) {
    // TODO(messick) See if we need to add a semicolon, like the Java implementation does.
    return copyFile;
  }
}
