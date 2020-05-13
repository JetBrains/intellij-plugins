// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopeUtil;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable;
import org.angular2.lang.html.psi.impl.Angular2HtmlAttrVariableImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Angular2LetType extends Angular2HtmlAttrVariableType {

  public static SearchScope getUseScope(Angular2HtmlAttrVariableImpl variable) {
    return GlobalSearchScope.filesScope(variable.getProject(),
                                        GlobalSearchScopeUtil.getLocalScopeFiles(new LocalSearchScope(variable.getContainingFile())));
  }

  public Angular2LetType(@NotNull Angular2HtmlAttrVariableImpl variable) {
    this(JSTypeSourceFactory.createTypeSource(variable, true));
  }

  protected Angular2LetType(@NotNull JSTypeSource source) {
    super(source);
    assert ((Angular2HtmlAttrVariableImpl)Objects.requireNonNull(source.getSourceElement()))
             .getKind() == Angular2HtmlAttrVariable.Kind.LET;
  }

  @Override
  protected @NotNull JSType copyWithNewSource(@NotNull JSTypeSource source) {
    return new Angular2LetType(source);
  }

  @Override
  @Nullable
  protected JSType resolveType() {
    return null;
  }
}
