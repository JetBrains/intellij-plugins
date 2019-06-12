package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2DirectiveAttribute;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2SourceDirectiveAttribute implements Angular2DirectiveAttribute {
  private final JSParameter myParameter;
  private final String myName;

  Angular2SourceDirectiveAttribute(@NotNull final JSParameter parameter,
                                   @NotNull final String bindingName) {
    myParameter = parameter;
    myName = bindingName;
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
    return myParameter;
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
