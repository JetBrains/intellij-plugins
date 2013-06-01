package com.jetbrains.lang.dart.ide.findUsages;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public class DartUsageTypeProvider implements UsageTypeProvider {
  @Override
  public UsageType getUsageType(PsiElement element) {
    if (!(element instanceof DartReference)) {
      return null;
    }
    final PsiElement parent = element.getParent();
    if (parent instanceof DartType) {
      return getUsageTypeByType((DartType)parent);
    }

    if (DartResolveUtil.isLValue(element)) {
      return UsageType.WRITE;
    }

    if (!(parent instanceof DartReference)) {
      return UsageType.READ;
    }
    final PsiElement target = ((DartReference)element).resolve();
    final DartComponentType targetType = DartComponentType.typeOf(target == null ? null : target.getParent());
    final boolean isTargetClassOrInterface = targetType == DartComponentType.CLASS || targetType == DartComponentType.INTERFACE;

    final DartReference[] references = PsiTreeUtil.getChildrenOfType(parent, DartReference.class);
    final boolean isFirstChild = references != null && references.length > 0 && references[0] == element;
    if (isFirstChild && references.length == 2 && isTargetClassOrInterface) {
      return UsageType.CLASS_STATIC_MEMBER_ACCESS;
    }

    if (isFirstChild) {
      return UsageType.READ;
    }
    if (!(parent.getParent() instanceof DartCallExpression)) {
      return UsageType.READ;
    }
    return null;
  }

  @Nullable
  private static UsageType getUsageTypeByType(DartType type) {
    final PsiElement typeParent = type.getParent();
    final DartComponentType typeParentType = DartComponentType.typeOf(typeParent);
    if (typeParent instanceof DartSuperclass) {
      return UsageType.CLASS_EXTENDS_IMPLEMENTS_LIST;
    }
    if (typeParent instanceof DartNewExpression) {
      return UsageType.CLASS_NEW_OPERATOR;
    }
    if (typeParent instanceof DartTypeList && typeParent.getParent() instanceof DartInterfaces) {
      return UsageType.CLASS_EXTENDS_IMPLEMENTS_LIST;
    }
    if (typeParent instanceof DartTypeList && typeParent.getParent() instanceof DartTypeArguments) {
      return UsageType.TYPE_PARAMETER;
    }
    if (typeParent instanceof DartReturnType) {
      return UsageType.CLASS_METHOD_RETURN_TYPE;
    }
    if (typeParent instanceof DartVarAccessDeclaration && typeParentType != null) {
      switch (typeParentType) {
        case PARAMETER:
          return UsageType.CLASS_METHOD_PARAMETER_DECLARATION;
        case FIELD:
          return UsageType.CLASS_FIELD_DECLARATION;
        case METHOD:
          return UsageType.CLASS_METHOD_RETURN_TYPE;
        default:
          return null;
      }
    }
    return null;
  }
}
