package com.jetbrains.lang.dart.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DartControlFlowUtil {
  public static Set<DartComponentName> getSimpleDeclarations(PsiElement[] children,
                                                             @Nullable PsiElement lastParent,
                                                             boolean stopAtLastParent) {
    final Set<DartComponentName> result = new THashSet<>();
    boolean addComponentsFlag = true;
    for (PsiElement child : children) {
      if (child == lastParent && stopAtLastParent) {
        addComponentsFlag = false;
      }

      if (addComponentsFlag && child instanceof DartVarDeclarationList) {
        addFromVarDeclarationList(result, (DartVarDeclarationList)child);
      }
      else if (child instanceof DartComponent) {
        boolean isFieldOrVar = child instanceof DartVarAccessDeclaration || child instanceof DartVarDeclarationListPart;
        if (!isFieldOrVar) {
          result.add(((DartComponent)child).getComponentName());
        }
      }
    }
    return result;
  }

  public static void addFromVarDeclarationList(Set<DartComponentName> result, DartVarDeclarationList declarationList) {
    result.add(declarationList.getVarAccessDeclaration().getComponentName());
    for (DartVarDeclarationListPart declarationListPart : declarationList.getVarDeclarationListPartList()) {
      result.add(declarationListPart.getComponentName());
    }
  }
}
