package com.jetbrains.lang.dart.ide.template.postfix;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.hash.HashMap;
import com.intellij.util.containers.hash.HashSet;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.PostfixCompletionTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class DartPostfixTemplateProvider implements PostfixTemplateProvider {
  private static final String UNINITIALIZED_KEY = "none";
  private static Map<String, Set<PostfixTemplate>> templateCache = new HashMap<>();

  static {
    Set<PostfixTemplate> one = new HashSet<>();
    one.add(new DartRemotePostfixTemplate("none", ".none", "none"));
    // Prime the cache to enable postfix completion UI in Preferences before any analysis server is started.
    templateCache.put(UNINITIALIZED_KEY, one);
  }

  public static Set<PostfixTemplate> getTemplates(String version) {
    return templateCache.get(version);
  }

  public static boolean initializeTemplates(DartAnalysisServerService service) {
    String version = service.getSdkVersion();
    if (templateCache.get(version) != null) {
      return true;
    }
    PostfixCompletionTemplate[] templates = service.edit_listPostfixCompletionTemplates();
    Set<PostfixTemplate> set = new HashSet<>();
    if (templates != null) {
      try {
        for (PostfixCompletionTemplate template : templates) {
          set.add(DartRemotePostfixTemplate.createTemplate(template));
        }
        templateCache.put(version, set);
        return true;
      }
      catch (Exception ex) {
        Logger log = Logger.getInstance("#com.jetbrains.lang.dart.ide.template.postfix.DartPostfixTemplateProvider");
        log.error(ex);
      }
    }
    return false;
  }

  @NotNull
  @Override
  public Set<PostfixTemplate> getTemplates() {
    // Find the largest initialized set. This may return templates that are not recognized
    // by the analysis server actually used for expansion, but at least the user will see
    // all possible templates.
    Set<PostfixTemplate> set = templateCache.get(UNINITIALIZED_KEY);
    for (Set<PostfixTemplate> cache : templateCache.values()) {
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
