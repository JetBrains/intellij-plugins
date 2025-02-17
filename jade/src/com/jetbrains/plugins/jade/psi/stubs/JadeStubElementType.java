package com.jetbrains.plugins.jade.psi.stubs;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.jetbrains.plugins.jade.JadeLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class JadeStubElementType<S extends StubElement<?>, T extends StubBasedPsiElementBase<?>>
  extends IStubElementType<S, T> {

  public JadeStubElementType(@NotNull @NonNls String debugName) {
    super(debugName, JadeLanguage.INSTANCE);
  }

  @Override
  public String toString() {
    return "Jade: " + super.toString();
  }

  @Override
  public @NotNull String getExternalId() {
    return "Jade." + super.toString();
  }
}

