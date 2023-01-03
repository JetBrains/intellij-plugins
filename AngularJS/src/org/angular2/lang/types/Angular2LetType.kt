// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext;
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory;
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopeUtil;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable;
import org.angular2.lang.html.psi.impl.Angular2HtmlAttrVariableImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;
import static org.angular2.lang.Angular2LangUtil.$IMPLICIT;

public class Angular2LetType extends Angular2BaseType<Angular2HtmlAttrVariableImpl> {

  public static SearchScope getUseScope(Angular2HtmlAttrVariableImpl variable) {
    return GlobalSearchScope.filesScope(variable.getProject(),
                                        GlobalSearchScopeUtil.getLocalScopeFiles(new LocalSearchScope(variable.getContainingFile())));
  }

  public Angular2LetType(@NotNull Angular2HtmlAttrVariableImpl variable) {
    super(variable, Angular2HtmlAttrVariableImpl.class);
    assert variable.getKind() == Angular2HtmlAttrVariable.Kind.LET : variable;
  }

  protected Angular2LetType(@NotNull JSTypeSource source) {
    super(source, Angular2HtmlAttrVariableImpl.class);
    assert getSourceElement().getKind() == Angular2HtmlAttrVariable.Kind.LET : getSourceElement();
  }

  @Override
  protected @Nullable String getTypeOfText() {
    return doIfNotNull(PsiTreeUtil.getContextOfType(getSourceElement(), XmlAttribute.class), XmlAttribute::getName);
  }

  @Override
  protected @NotNull JSType copyWithNewSource(@NotNull JSTypeSource source) {
    return new Angular2LetType(source);
  }

  @Override
  protected @Nullable JSType resolveType(@NotNull JSTypeSubstitutionContext context) {
    XmlAttribute attribute = PsiTreeUtil.getContextOfType(getSourceElement(), XmlAttribute.class);
    XmlTag tag = PsiTreeUtil.getContextOfType(getSourceElement(), XmlTag.class);
    if (attribute == null || tag == null) {
      return null;
    }
    String contextItemName = notNull(attribute.getValue(), $IMPLICIT);
    JSType templateContext = Angular2TypeUtils.getNgTemplateTagContextType(tag);
    return templateContext != null ? JSCompositeTypeFactory.createIndexedAccessType(
      templateContext, new JSStringLiteralTypeImpl(contextItemName, false, getSource()), getSource()).substitute(context) : null;
  }
}
