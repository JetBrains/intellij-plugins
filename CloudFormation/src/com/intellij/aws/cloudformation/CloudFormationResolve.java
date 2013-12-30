package com.intellij.aws.cloudformation;

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class CloudFormationResolve {
  @Nullable
  public static JSObjectLiteralExpression getSectionNode(final PsiFile file, String name) {
    return CloudFormationPsiUtils.getObjectLiteralExpressionChild(CloudFormationPsiUtils.getRootExpression(file), name);
  }

  @Nullable
  public static PsiElement resolveEntity(PsiFile file, String entityName, String... sections) {
    for (String sectionName : sections) {
      final JSObjectLiteralExpression section = getSectionNode(file, sectionName);
      if (section != null) {
        final JSProperty property = section.findProperty(entityName);
        if (property != null) {
          return property;
        }
      }
    }

    return null;
  }

  public static String[] getEntities(PsiFile file, String[] sections) {
    Set<String> result = new HashSet<String>();

    for (String sectionName : sections) {
      final JSObjectLiteralExpression section = getSectionNode(file, sectionName);
      if (section != null) {
        for (JSProperty property : section.getProperties()) {
          result.add(property.getName());
        }
      }
    }

    return ArrayUtil.toStringArray(result);
  }
}
