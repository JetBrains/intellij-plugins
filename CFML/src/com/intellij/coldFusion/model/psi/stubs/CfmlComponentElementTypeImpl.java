package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.coldFusion.model.psi.impl.CfmlComponentImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 */
public class CfmlComponentElementTypeImpl extends CfmlComponentElementType {
  public CfmlComponentElementTypeImpl(@NotNull @NonNls final String debugName) {
    super(debugName, CfmlLanguage.INSTANCE);
  }

  @Override
  public CfmlComponent createPsi(@NotNull CfmlComponentStub stub) {
    return new CfmlComponentImpl(stub);
  }
}
