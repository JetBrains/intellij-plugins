package com.intellij.lang.javascript.intentions;

public class CreateGetterIntention extends JSCreateGetterIntention {

  @Override
  protected boolean isAvailableForECMA4() {
    return true;
  }

}
