package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeOwner;
import com.intellij.openapi.util.NotNullComputable;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2DirectiveAttribute;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;

public class Angular2MetadataDirectiveAttribute implements Angular2DirectiveAttribute {

  private final Supplier<? extends JSParameter> myParameterSupplier;
  private final NotNullComputable<? extends PsiElement> mySourceSupplier;
  private final String myName;

  Angular2MetadataDirectiveAttribute(final @NotNull Supplier<? extends JSParameter> parameterSupplier,
                                     final @NotNull NotNullComputable<? extends PsiElement> sourceSupplier,
                                     final @NotNull String name) {
    myParameterSupplier = parameterSupplier;
    mySourceSupplier = sourceSupplier;
    myName = name;
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @Nullable JSType getType() {
    return doIfNotNull(myParameterSupplier.get(), JSTypeOwner::getJSType);
  }

  @Override
  public @NotNull PsiElement getSourceElement() {
    return notNull(myParameterSupplier.get(), mySourceSupplier.compute());
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }
}
