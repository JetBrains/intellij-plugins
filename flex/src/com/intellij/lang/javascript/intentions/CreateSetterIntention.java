package com.intellij.lang.javascript.intentions;

public class CreateSetterIntention extends JSCreateSetterIntention {
  @Override
  protected boolean isAvailableForECMA4() {
    return true;
  }
}
