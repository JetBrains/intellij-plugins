package com.intellij.lang.javascript.linter.jshint.config;

import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.jshint.JSHintDocumentation;
import com.intellij.lang.javascript.linter.jshint.JSHintOption;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.ui.HintHint;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.Point;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JSHintConfigDocumentationProvider implements DocumentationProvider {

  @Override
  public @Nullable @Nls String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
    String result = null;
    if (element != null) {
      result = doGenerateDoc(element);
    }
    if (result == null && originalElement != null) {
      result = doGenerateDoc(originalElement.getParent());
    }
    return result;
  }

  public @Nullable @Nls String doGenerateDoc(@NotNull PsiElement element) {
    if (JSHintConfigFileUtil.isJSHintConfigFile(element)) {
      JsonProperty property = JSLinterConfigFileUtil.getProperty(element);
      if (property != null) {
        PsiElement keyElement = JSLinterConfigFileUtil.getFirstChildAsStringLiteral(property);
        if (keyElement == element) {
          String name = StringUtil.unquoteString(element.getText());
          JSHintOption option = JSHintOption.findByName(name);
          if (option != null) {
            JSHintDocumentation documentation = JSHintDocumentation.getInstance();
            String html = documentation.getHtmlDescriptionByOption(option);
            if (html == null) {
              return null;
            }
            final HintHint hintHint = new HintHint((Component)null, new Point(0, 0));
            return HintUtil.prepareHintText(html, hintHint);
          }
        }
      }
    }
    return null;
  }

  @Override
  public @Nullable PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
    if (object instanceof JSHintOptionCompletionObject option) {
      Project project = psiManager != null ? psiManager.getProject() : null;
      if (project != null) {
        return generateElement(project, option.getName());
      }
    }
    return null;
  }

  private static @Nullable PsiElement generateElement(@NotNull Project project, @NotNull String key) {
    String jsonText = "{" + StringUtil.wrapWithDoubleQuote(key) + ":\"\"}";
    PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(".jshintrc", JSHintConfigFileType.INSTANCE, jsonText);
    PsiElement[] topLevelChildren = psiFile.getChildren();
    JsonObject objectLiteralExpression = null;
    for (PsiElement child : topLevelChildren) {
      if (child instanceof JsonObject) {
        objectLiteralExpression = (JsonObject) child;
        break;
      }
    }
    if (objectLiteralExpression == null) {
      return null;
    }
    List<JsonProperty> properties = objectLiteralExpression.getPropertyList();
    for (JsonProperty property : properties) {
      PsiElement psiElement = JSLinterConfigFileUtil.getFirstChildAsStringLiteral(property);
      if (psiElement != null) {
        String name = StringUtil.unquoteString(psiElement.getText());
        if (key.equals(name)) {
          return psiElement;
        }
      }
    }
    return null;
  }
}
