/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.struts2.freemarker;

import com.intellij.freemarker.FreeMarkerApplicationComponent;
import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.struts2.dom.struts.impl.path.StrutsResultContributor;
import com.intellij.util.ConstantFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author peter
 */
public class FreeMarkerStrutsResultContributor extends StrutsResultContributor {

  @Override
  protected boolean matchesResultType(@NonNls @Nullable String resultType) {
    return "freemarker".equalsIgnoreCase(resultType);
  }

  public boolean createReferences(@NotNull PsiElement psiElement, @NotNull List<PsiReference> references, boolean soft) {
    final FileReferenceSet set = FileReferenceSet.createSet(psiElement, soft, false, true);
    if (set == null) {
      return false;
    }

    if (getNamespace(psiElement) == null) {
      return false;
    }

    ContainerUtil.addAll(references, set.getAllReferences());
    return true;
  }

  public PathReference getPathReference(@NotNull String path, @NotNull PsiElement element) {
    if (getNamespace(element) == null) {
      return null;
    }

    final ArrayList<PsiReference> list = new ArrayList<PsiReference>(5);
    createReferences(element, list, true);
    if (list.isEmpty()) return null;

    final PsiElement target = list.get(list.size() - 1).resolve();
    if (target == null) return null;

    return new PathReference(path, new ConstantFunction<PathReference, Icon>(FreeMarkerApplicationComponent.FREEMARKER_ICON)) {
      @Override
      public PsiElement resolve() {
        return target;
      }
    };
  }
}
