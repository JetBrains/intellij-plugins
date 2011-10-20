package com.intellij.flex.uiDesigner {
public final class ElementAddress {
  public var factory:DocumentFactory;
  public var offset:int;

  public function ElementAddress(factory:DocumentFactory, offset:int) {
    this.factory = factory;
    this.offset = offset;
  }
}
}