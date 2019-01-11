// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css.findUsages;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.usages.CssClassOrIdReferenceBasedUsagesProvider;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;

public class Angular2CssClassOrIdUsagesProvider extends CssClassOrIdReferenceBasedUsagesProvider {

  @Override
  protected boolean acceptElement(@NotNull PsiElement candidate) {
    return candidate instanceof XmlAttribute && Angular2LangUtil.isAngular2Context(candidate);
  }
}
