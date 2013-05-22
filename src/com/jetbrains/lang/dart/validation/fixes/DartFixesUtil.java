package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.util.DartResolveUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartFixesUtil {
  public static boolean isStaticContext(PsiFile file, int startOffset) {
    DartReference leftReference =
      DartResolveUtil.getLeftReference(PsiTreeUtil.getParentOfType(file.findElementAt(startOffset), DartReference.class));
    return leftReference != null && DartComponentType.typeOf(leftReference.resolve()) == DartComponentType.CLASS;
  }

  public static List<? extends IntentionAction> findFixesForUnresolved(PsiFile file, int startOffset) {
    final DartReference reference = PsiTreeUtil.getParentOfType(file.findElementAt(startOffset), DartReference.class);
    final DartClass dartClass = PsiTreeUtil.getParentOfType(reference, DartClass.class);
    final String name = reference != null ? reference.getText() : null;
    if (reference == null || name == null) {
      return Collections.emptyList();
    }
    final boolean isLValue = DartResolveUtil.isLValue(reference);
    final DartReference leftReference = DartResolveUtil.getLeftReference(reference);

    final ArrayList<IntentionAction> result = new ArrayList<IntentionAction>();

    // chain
    if (leftReference != null) {
      final PsiElement leftTarget = leftReference.resolve();
      final DartComponentType leftTargetType = DartComponentType.typeOf(leftTarget != null ? leftTarget.getParent() : null);
      result.add(new CreateDartGetterSetterAction(name, !isLValue, leftTargetType == DartComponentType.CLASS));
      result.add(new CreateFieldAction(name, leftTargetType == DartComponentType.CLASS));
      if (DartResolveUtil.aloneOrFirstInChain(reference)) {
        result.add(new CreateGlobalDartGetterSetterAction(name, false));
      }
    }

    // alone with capital. seems class name.
    if (DartResolveUtil.aloneOrFirstInChain(reference) && StringUtil.isCapitalized(name)) {
      result.add(new CreateDartClassAction(name));
    }

    // alone. not class.
    if (DartResolveUtil.aloneOrFirstInChain(reference) && !StringUtil.isCapitalized(name)) {
      final DartComponent parentComponent = PsiTreeUtil.getParentOfType(reference, DartComponent.class);
      if (reference.getParent() instanceof DartCallExpression) {
        result.add(new CreateGlobalDartFunctionAction(name));
        if (dartClass != null) {
          result.add(new CreateDartMethodAction(name, parentComponent != null && parentComponent.isStatic()));
        }
      }
      else {
        result.add(new CreateDartGetterSetterAction(name, !isLValue, parentComponent != null && parentComponent.isStatic()));
        result.add(new CreateGlobalDartGetterSetterAction(name, !isLValue));
        result.add(new CreateFieldAction(name, parentComponent != null && parentComponent.isStatic()));
        result.add(new CreateGlobalVariableAction(name));
        result.add(new CreateLocalVariableAction(name));
      }
    }

    return result;
  }
}
