// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class DartControlFlowUtil {
  public static Set<DartComponentName> getSimpleDeclarations(PsiElement[] children,
                                                             @Nullable PsiElement lastParent,
                                                             boolean stopAtLastParent) {
    final Set<DartComponentName> result = new HashSet<>();
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

  public static void addFromVarDeclarationList(Set<? super DartComponentName> result, DartVarDeclarationList declarationList) {
    result.add(declarationList.getVarAccessDeclaration().getComponentName());
    for (DartVarDeclarationListPart declarationListPart : declarationList.getVarDeclarationListPartList()) {
      result.add(declarationListPart.getComponentName());
    }
  }
}
