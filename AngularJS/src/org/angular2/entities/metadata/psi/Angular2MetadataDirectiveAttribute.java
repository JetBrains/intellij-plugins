package org.angular2.entities.metadata.psi;

import org.angular2.entities.Angular2DirectiveAttribute;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.psi.PsiElement;

public class Angular2MetadataDirectiveAttribute implements Angular2DirectiveAttribute {
  private final JSParameter myParameter;
  private final String myBindingName;

  Angular2MetadataDirectiveAttribute(
        @NotNull final JSParameter parameter,
        @NotNull final String bindingName) {
    myParameter = parameter;
    myBindingName = bindingName;
  }

  @NotNull
  @Override
  public String getName() {
    return myBindingName;
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
