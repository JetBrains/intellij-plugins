package org.angular2.entities.metadata.psi;

import org.angular2.entities.Angular2DirectiveAttribute;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.psi.PsiElement;

public class Angular2MetadataDirectiveAttribute implements Angular2DirectiveAttribute {
  private final String bindingName;
  private final JSParameter parameter;

  Angular2MetadataDirectiveAttribute(
        @NotNull final String bindingName,
        @NotNull final JSParameter parameter) {
    this.bindingName = bindingName;
    this.parameter = parameter;
  }

  @NotNull
  @Override
  public String getName() {
    return bindingName;
  }

  @Nullable
  @Override
  public JSType getType() {
    return parameter.getJSType();
  }

  @NotNull
  @Override
  public PsiElement getSourceElement() {
    return parameter.getDeclarationElement();
  }

  @NotNull
  @Override
  public PsiElement getNavigableElement() {
    return parameter.getNavigationElement();
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }
}
