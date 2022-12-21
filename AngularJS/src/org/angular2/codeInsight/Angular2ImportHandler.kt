// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.ecmascript6.TypeScriptImportHandler;
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedNameResolver;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.resolve.JSImportHandler;
import com.intellij.lang.javascript.psi.resolve.JSTypeResolveResult;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2ComponentLocator;
import org.jetbrains.annotations.NotNull;

public class Angular2ImportHandler extends TypeScriptImportHandler {

  private static final JSImportHandler JS_IMPORT_HANDLER = JSImportHandler.getInstance();

  @Override
  protected @NotNull JSTypeResolveResult resolveNameImpl(@NotNull String type,
                                                         @NotNull PsiElement sourceRaw,
                                                         @NotNull TypeScriptQualifiedNameResolver.StrictKind typeContext,
                                                         boolean includeAugmentations) {
    TypeScriptClass cls = Angular2ComponentLocator.findComponentClass(sourceRaw);
    if (cls != null) {
      return super.resolveNameImpl(type, cls, typeContext, includeAugmentations);
    }
    else {
      return JS_IMPORT_HANDLER.resolveName(type, sourceRaw);
    }
  }
}
