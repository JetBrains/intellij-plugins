package com.intellij.aws.cloudformation;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.json.JSONLanguageDialect;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.Nullable;

public class CloudFormationPsiUtils {
  public static boolean isCloudFormationFile(final PsiElement element) {
    final PsiFile containingFile = element.getContainingFile();
    if (!(containingFile instanceof JSFile)) {
      return false;
    }

    final JSFile jsFile = (JSFile)containingFile;

    Language language = jsFile.getLanguage();
    if (language.isKindOf(JavascriptLanguage.INSTANCE)) {
      JSLanguageDialect dialect = JSUtils.getDialect(jsFile);

      if (dialect == JSONLanguageDialect.JSON) {
        final JSObjectLiteralExpression root = getRootExpression(jsFile);
        if (root != null) {
          return root.findProperty(CloudFormationSections.FormatVersion) != null;
        }
      }
    }

    return false;
  }

  @Nullable
  public static JSObjectLiteralExpression getRootExpression(final PsiFile file) {
    for (PsiElement cur = file.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
      if (cur instanceof JSObjectLiteralExpression) {
        return (JSObjectLiteralExpression)cur;
      }
    }

    return null;
  }

  @Nullable
  public static JSObjectLiteralExpression getObjectLiteralExpressionChild(@Nullable JSObjectLiteralExpression parent, String childName) {
    if (parent == null) {
      return null;
    }

    final JSProperty property = parent.findProperty(childName);
    if (property == null) {
      return null;
    }

    return ObjectUtils.tryCast(property.getValue(), JSObjectLiteralExpression.class);
  }
}
