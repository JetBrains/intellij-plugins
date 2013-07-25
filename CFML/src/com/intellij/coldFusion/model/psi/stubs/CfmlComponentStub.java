package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.psi.stubs.NamedStub;

/**
 * @author vnikolaenko
 */
public interface CfmlComponentStub extends NamedStub<CfmlComponent> {
  String getSuperclass();
  String[] getInterfaces();
  boolean isInterface();
}
