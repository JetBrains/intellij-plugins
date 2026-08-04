package com.intellij.lang.javascript.psi.stubs.impl;

import com.intellij.lang.javascript.index.JSIndexKeys;
import com.intellij.lang.javascript.psi.JSElementType;
import com.intellij.lang.javascript.psi.ecmal4.JSNamespaceDeclaration;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSNamespaceDeclarationImpl;
import com.intellij.lang.javascript.psi.stubs.JSNamespaceDeclarationStub;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Maxim.Mossienko
 */
public final class JSNamespaceDeclarationStubImpl extends JSQualifiedObjectStubBase<JSNamespaceDeclaration> implements JSNamespaceDeclarationStub {
  private final String myInitialValueString;

  public JSNamespaceDeclarationStubImpl(final JSNamespaceDeclaration psi,
                                        final StubElement parentStub,
                                        @NotNull JSElementType<JSNamespaceDeclaration> type) {
    super(psi, parentStub, type, 0);
    myInitialValueString = psi.getInitialValueString();
  }

  public JSNamespaceDeclarationStubImpl(final StubInputStream dataStream, final StubElement parentStub,
                                        @NotNull JSElementType<JSNamespaceDeclaration> type) throws IOException {
    super(dataStream,parentStub, type);
    myInitialValueString = dataStream.readNameString();
  }

  @Override
  public JSNamespaceDeclaration createPsi() {
    return new JSNamespaceDeclarationImpl(this);
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    String name = getName();
    if (name != null && doIndexName()) {
      sink.occurrence(JSIndexKeys.JS_NAME_INDEX_KEY, name);
    }
  }

  private boolean doIndexName() {
    return isUnderPackageOrUnderFile(this);
  }

  @Override
  protected boolean doIndexQualifiedName() {
    return doIndexName();
  }

  @Override
  public void serialize(final StubOutputStream dataStream) throws IOException {
    super.serialize(dataStream);
    dataStream.writeName(myInitialValueString);
  }

  @Override
  public String getInitialValueString() {
    return myInitialValueString;
  }
}
