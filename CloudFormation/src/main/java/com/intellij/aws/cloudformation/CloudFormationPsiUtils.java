package com.intellij.aws.cloudformation;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.Nullable;

public class CloudFormationPsiUtils {
  public static boolean isCloudFormationFile(final PsiElement element) {
    final PsiFile containingFile = element.getContainingFile();
    if (!(containingFile instanceof JsonFile)) {
      return false;
    }

    final JsonFile jsFile = (JsonFile) containingFile;
    final JsonObject root = getRootExpression(jsFile);
    return root != null && root.findProperty(CloudFormationSections.FormatVersion) != null;
  }

  @Nullable
  public static JsonObject getRootExpression(final PsiFile file) {
    for (PsiElement cur = file.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
      if (cur instanceof JsonObject) {
        return (JsonObject)cur;
      }
    }

    return null;
  }

  @Nullable
  public static JsonObject getObjectLiteralExpressionChild(@Nullable JsonObject parent, String childName) {
    if (parent == null) {
      return null;
    }

    final JsonProperty property = parent.findProperty(childName);
    if (property == null) {
      return null;
    }

    return ObjectUtils.tryCast(property.getValue(), JsonObject.class);
  }
}
