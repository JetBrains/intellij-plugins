package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.CfmlPsiElement;
import com.intellij.lang.Language;
import com.intellij.psi.stubs.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;

/**
 * @author vnikolaenko
 */
public abstract class CfmlStubElementType<S extends StubElement, T extends CfmlPsiElement> extends IStubElementType<S, T> {
  public CfmlStubElementType(@NotNull @NonNls final String debugName, @Nullable final Language language) {
    super(debugName, CfmlLanguage.INSTANCE);
  }

  @NotNull
  public String getExternalId() {
    return "cfml." + super.toString();
  }

  public void indexStub(@NotNull S stub, @NotNull IndexSink sink) {
  }
}
