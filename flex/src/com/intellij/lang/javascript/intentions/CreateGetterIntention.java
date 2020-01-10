package com.intellij.lang.javascript.intentions;

import com.intellij.lang.javascript.DialectOptionHolder;
import org.jetbrains.annotations.Nullable;

public class CreateGetterIntention extends JSCreateGetterIntention {
  @Override
  protected boolean isAvailableForDialect(@Nullable DialectOptionHolder dialectOfElement) {
    return dialectOfElement != null && dialectOfElement.isECMA4;
  }
}
