package org.angular2.entities.metadata.psi;

import org.angular2.entities.Angular2DirectiveAttribute;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.psi.PsiElement;

public class Angular2MetadataDirectiveAttribute implements Angular2DirectiveAttribute {
  private final String myBindingName;
  private final JSParameter myParameter;

  Angular2MetadataDirectiveAttribute(
        @NotNull final String bindingName,
        @NotNull final JSParameter parameter) {
    myBindingName = bindingName;
    myParameter = parameter;
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
