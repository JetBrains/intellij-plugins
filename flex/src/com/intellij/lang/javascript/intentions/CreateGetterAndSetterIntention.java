package com.intellij.lang.javascript.intentions;


import com.intellij.lang.javascript.DialectOptionHolder;
import org.jetbrains.annotations.Nullable;

public class CreateGetterAndSetterIntention extends JSCreateGetterAndSetterIntention{
  @Override
  protected boolean isAvailableForDialect(@Nullable DialectOptionHolder dialectOfElement) {
    return dialectOfElement != null && dialectOfElement.isECMA4;
  }
}
