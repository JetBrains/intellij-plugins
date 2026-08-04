package com.intellij.lang.javascript.psi.stubs.impl;

import com.intellij.lang.javascript.psi.JSElementType;
import com.intellij.lang.javascript.psi.ecmal4.JSUseNamespaceDirective;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSUseNamespaceDirectiveImpl;
import com.intellij.lang.javascript.psi.stubs.JSUseNamespaceDirectiveStub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Maxim.Mossienko
 */
public final class JSUseNamespaceDirectiveStubImpl extends JSStubBase<JSUseNamespaceDirective> implements JSUseNamespaceDirectiveStub {
  private final @Nullable String myNamespaceToUse;

  public JSUseNamespaceDirectiveStubImpl(final StubInputStream dataStream, final StubElement parentStub,
                                         @NotNull JSElementType<JSUseNamespaceDirective> type) throws IOException {
    super(dataStream, parentStub, type);
    myNamespaceToUse = dataStream.readNameString();
  }

  public JSUseNamespaceDirectiveStubImpl(final JSUseNamespaceDirective psi,
                                        final StubElement parentStub,
                                        @NotNull JSElementType<JSUseNamespaceDirective> type) {
    super(psi, parentStub, type);

    myNamespaceToUse = psi.getNamespaceToBeUsed();
  }

  @Override
  public JSUseNamespaceDirective createPsi() {
    return new JSUseNamespaceDirectiveImpl(this);
  }
  
  @Override
  public void serialize(final StubOutputStream dataStream) throws IOException {
    super.serialize(dataStream);
    dataStream.writeName(myNamespaceToUse);
  }

  @Override
  public @Nullable String getNamespaceToUse() {
    return myNamespaceToUse;
  }
}
