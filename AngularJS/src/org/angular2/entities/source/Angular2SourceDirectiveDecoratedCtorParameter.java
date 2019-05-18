package org.angular2.entities.source;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.angular2.entities.Angular2DirectiveCtorParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.psi.PsiElement;

/**
 * Represents a class decorated constructor parameter.
 */
public class Angular2SourceDirectiveDecoratedCtorParameter implements Angular2DirectiveCtorParameter {
  private final String myName;
  private final JSParameter myParameter;

  Angular2SourceDirectiveDecoratedCtorParameter(
        @NotNull final String bindingName,
        @NotNull final JSParameter parameter) {
    myName = bindingName;
    myParameter = parameter;
  }

  @NotNull
  @Override
  public String getName() {
    return myName;
  }

  @Nullable
  @Override
  public JSType getType() {
    return myParameter.getJSType();
  }

  @NotNull
  @Override
  public PsiElement getSourceElement() {
    return myParameter.getDeclarationElement();
  }

  @NotNull
  @Override
  public PsiElement getNavigableElement() {
    return myParameter.getNavigationElement();
  }

  @NotNull
  public Collection<ES6Decorator> getDecorators() {
    final JSAttributeList attributeList = myParameter.getAttributeList();
    return Objects.isNull(attributeList) ?
          Collections.emptyList() :
          Collections.unmodifiableCollection(Arrays.asList(attributeList.getDecorators()));
  }
}
