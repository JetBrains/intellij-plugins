package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.model.Pointer;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2DirectiveAttribute;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.intellij.refactoring.suggested.UtilsKt.createSmartPointer;

public class Angular2SourceDirectiveAttribute implements Angular2DirectiveAttribute {
  private final JSParameter myParameter;
  private final String myName;

  Angular2SourceDirectiveAttribute(final @NotNull JSParameter parameter,
                                   final @NotNull String bindingName) {
    myParameter = parameter;
    myName = bindingName;
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @Nullable JSType getJsType() {
    return myParameter.getJSType();
  }

  @Override
  public @NotNull PsiElement getSourceElement() {
    return myParameter;
  }

  @Override
  public @NotNull PsiElement getNavigableElement() {
    return myParameter.getNavigationElement();
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2SourceDirectiveAttribute attribute = (Angular2SourceDirectiveAttribute)o;
    return myParameter.equals(attribute.myParameter)
           && myName.equals(attribute.myName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myParameter, myName);
  }

  @NotNull
  @Override
  public Pointer<Angular2SourceDirectiveAttribute> createPointer() {
    var name = myName;
    var parameter = createSmartPointer(myParameter);
    return () -> {
      var newParameter = parameter.getElement();
      return newParameter != null ? new Angular2SourceDirectiveAttribute(newParameter, name) : null;
    };
  }
}
