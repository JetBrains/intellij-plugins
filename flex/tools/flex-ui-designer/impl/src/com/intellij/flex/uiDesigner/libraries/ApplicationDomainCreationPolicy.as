package com.intellij.flex.uiDesigner.libraries {
import cocoa.lang.Enum;

public final class ApplicationDomainCreationPolicy extends Enum {
  public static const ONE:ApplicationDomainCreationPolicy = new ApplicationDomainCreationPolicy("one", 0);
  public static const MULTIPLE:ApplicationDomainCreationPolicy = new ApplicationDomainCreationPolicy("multiple", 1);

  public static const enumSet:Vector.<ApplicationDomainCreationPolicy> = new <ApplicationDomainCreationPolicy>[ONE, MULTIPLE];

  public function ApplicationDomainCreationPolicy(name:String, ordinal:int = -1) {
    super(name, ordinal);
  }
}
}