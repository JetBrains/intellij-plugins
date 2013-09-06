package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.index.DartComponentIndex;
import com.jetbrains.lang.dart.ide.index.DartComponentInfo;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartFixesUtil {
  public static boolean isStaticContext(PsiFile file, int startOffset) {
    DartReference leftReference =
      DartResolveUtil.getLeftReference(PsiTreeUtil.getParentOfType(file.findElementAt(startOffset), DartReference.class));
    return leftReference != null && DartComponentType.typeOf(leftReference.resolve()) == DartComponentType.CLASS;
  }

  public static List<BaseCreateFix> findFixesForUnresolved(PsiFile file, int startOffset) {
    final DartReference reference = PsiTreeUtil.getParentOfType(file.findElementAt(startOffset), DartReference.class);
    final String name = reference != null ? reference.getText() : null;
    if (reference == null || name == null) {
      return Collections.emptyList();
    }
    final boolean isLValue = DartResolveUtil.isLValue(reference);
    final DartReference leftReference = DartResolveUtil.getLeftReference(reference);

    final List<BaseCreateFix> result = new ArrayList<BaseCreateFix>();

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
        if (PsiTreeUtil.getParentOfType(reference, DartClass.class) != null) {
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

    if (DartResolveUtil.aloneOrFirstInChain(reference) && !StringUtil.startsWithChar(name, '_')) {
      suggestImports(result, reference, name);
    }

    return result;
  }

  public static void suggestImports(final List<BaseCreateFix> result, @NotNull PsiElement context, @Nls final String name) {
    DartComponentIndex.processComponentsByName(context, new Processor<DartComponentInfo>() {
      @Override
      public boolean process(DartComponentInfo info) {
        final DartComponentType componentType = info.getType();
        if (componentType == DartComponentType.CLASS ||
            componentType == DartComponentType.INTERFACE ||
            componentType == DartComponentType.FUNCTION ||
            componentType == DartComponentType.VARIABLE) {
          result.add(new DartImportFix(name, info));
        }
        return true;
      }
    }, name);
  }
}
