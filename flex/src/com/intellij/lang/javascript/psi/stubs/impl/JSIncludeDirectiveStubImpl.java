package com.intellij.lang.javascript.psi.stubs.impl;

import com.intellij.lang.javascript.psi.JSElementType;
import com.intellij.lang.javascript.psi.ecmal4.JSIncludeDirective;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSIncludeDirectiveImpl;
import com.intellij.lang.javascript.psi.stubs.JSIncludeDirectiveStub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Maxim.Mossienko
 */
public final class JSIncludeDirectiveStubImpl extends JSStubBase<JSIncludeDirective> implements JSIncludeDirectiveStub {
  private final String myIncludeText;

  public JSIncludeDirectiveStubImpl(final StubInputStream dataStream, final StubElement parentStub,
                                    @NotNull JSElementType<JSIncludeDirective> type) throws IOException {
    super(dataStream, parentStub, type);
    myIncludeText = dataStream.readNameString();
  }

  public JSIncludeDirectiveStubImpl(final JSIncludeDirective psi,
                                        final StubElement parentStub,
                                        @NotNull JSElementType<JSIncludeDirective> type) {
    super(psi, parentStub, type);
    myIncludeText = psi.getIncludeText();
  }

  @Override
  public JSIncludeDirective createPsi() {
    return new JSIncludeDirectiveImpl(this);
  }
  
  @Override
  public void serialize(final StubOutputStream dataStream) throws IOException {
    super.serialize(dataStream);
    dataStream.writeName(myIncludeText);
  }

  @Override
  public String getIncludeText() {
    return myIncludeText;
  }
}
