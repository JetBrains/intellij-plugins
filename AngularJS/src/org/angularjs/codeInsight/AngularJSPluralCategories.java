package org.angularjs.codeInsight;

public enum AngularJSPluralCategories {
  zero(5),
  one(1),
  two(4),
  few(3),
  many(2),
  other(0);

  private final int myCompletionOrder;

  AngularJSPluralCategories(int completionOrder) {
    myCompletionOrder = completionOrder;
  }

  public int getCompletionOrder() {
    return myCompletionOrder;
  }
}
