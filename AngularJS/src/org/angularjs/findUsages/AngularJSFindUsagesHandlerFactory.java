// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.findUsages;

import com.intellij.lang.javascript.findUsages.JavaScriptFindUsagesHandlerFactory;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElement;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;

public final class AngularJSFindUsagesHandlerFactory extends JavaScriptFindUsagesHandlerFactory {

    @Override
    public boolean canFindUsages(@NotNull PsiElement element) {
        return element instanceof JSClass
                || DirectiveUtil.getDirective(element) != null;
    }
}
