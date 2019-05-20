package org.angular2.entities.source;

import org.angular2.entities.Angular2DirectiveAttribute;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.psi.PsiElement;

/**
 * Represents a class constructor {@code @Attribute} decorated parameter.
 */
public class Angular2SourceDirectiveAttribute implements Angular2DirectiveAttribute {
  private final String myName;
  private final JSParameter myParameter;

  Angular2SourceDirectiveAttribute(
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

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }
}
