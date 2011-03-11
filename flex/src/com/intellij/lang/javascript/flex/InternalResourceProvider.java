package com.intellij.lang.javascript.flex;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeImpl;

public class InternalResourceProvider implements StandardResourceProvider {
  public void registerResources(ResourceRegistrar registrar) {
    registrar.addStdResource(JSAttributeImpl.URN_FLEX_META, "KnownMetaData.dtd", getClass());
  }
}