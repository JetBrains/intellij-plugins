// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi;

import com.intellij.ide.presentation.Presentation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTarget;

@Presentation(typeName = DroolsQuery.QUERY, icon = "AllIcons.Nodes.Method")
public interface DroolsQuery extends DroolsPsiCompositeElement, PsiMethod, PsiTarget {
   String QUERY = "Query";
   String getQueryName();
}
