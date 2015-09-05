package com.intellij.lang.javascript.intentions;


public class CreateGetterAndSetterIntention extends JSCreateGetterAndSetterIntention{
  @Override
  protected boolean isAvailableForECMA4() {
    return true;
  }
}
