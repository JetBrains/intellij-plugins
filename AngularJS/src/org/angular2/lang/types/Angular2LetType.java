// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopeUtil;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable;
import org.angular2.lang.html.psi.impl.Angular2HtmlAttrVariableImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public class Angular2LetType extends Angular2BaseType<Angular2HtmlAttrVariableImpl> {

  public static SearchScope getUseScope(Angular2HtmlAttrVariableImpl variable) {
    return GlobalSearchScope.filesScope(variable.getProject(),
                                        GlobalSearchScopeUtil.getLocalScopeFiles(new LocalSearchScope(variable.getContainingFile())));
  }

  public Angular2LetType(@NotNull Angular2HtmlAttrVariableImpl variable) {
    super(variable);
  }

  protected Angular2LetType(@NotNull JSTypeSource source) {
    super(source);
  }

  @Override
  protected void validateSourceElement(@NotNull Angular2HtmlAttrVariableImpl element) {
    assert element.getKind() == Angular2HtmlAttrVariable.Kind.LET;
  }

  @Override
  protected @Nullable String getTypeOfText() {
    return doIfNotNull(PsiTreeUtil.findFirstParent(getSourceElement(), XmlAttribute.class::isInstance),
                       attr -> ((XmlAttribute)attr).getName());
  }

  @Override
  protected @NotNull JSType copyWithNewSource(@NotNull JSTypeSource source) {
    return new Angular2LetType(source);
  }

  @Override
  protected @Nullable JSType resolveType() {
    return null;
  }
}
