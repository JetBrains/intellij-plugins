// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext;
import com.intellij.lang.javascript.psi.JSTypeTextBuilder;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSCodeBasedType;
import com.intellij.lang.javascript.psi.types.JSSimpleTypeBaseImpl;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;

public abstract class Angular2HtmlAttrVariableType extends JSSimpleTypeBaseImpl implements JSCodeBasedType {

  protected Angular2HtmlAttrVariableType(@NotNull JSTypeSource source) {
    super(source);
  }

  @Override
  protected @Nullable JSType substituteImpl(@NotNull JSTypeSubstitutionContext context) {
    return notNull(resolveType(), () -> JSAnyType.get(getSource()));
  }

  @Override
  protected boolean isEquivalentToWithSameClass(@NotNull JSType type, @Nullable ProcessingContext context, boolean allowResolve) {
    return type.getClass() == this.getClass()
           && Objects.equals(type.getSourceElement(), getSourceElement());
  }

  @Override
  protected int resolvedHashCodeImpl() {
    return Objects.hashCode(getSourceElement());
  }

  @Override
  protected void buildTypeTextImpl(@NotNull TypeTextFormat format, @NotNull JSTypeTextBuilder builder) {
    if (format == TypeTextFormat.SIMPLE) {
      builder.append("typeof#" + doIfNotNull(PsiTreeUtil.findFirstParent(getSourceElement(), XmlAttribute.class::isInstance),
                                             attr -> ((XmlAttribute)attr).getName()));
      return;
    }
    substitute().buildTypeText(format, builder);
  }

  @Nullable
  protected abstract JSType resolveType();
}
