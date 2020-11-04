// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.stubs.*;
import com.intellij.psi.tree.ICompositeElementType;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector;
import org.angular2.lang.html.psi.impl.Angular2HtmlNgContentSelectorImpl;
import org.angular2.lang.html.stub.impl.Angular2HtmlNgContentSelectorStubImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class Angular2HtmlNgContentSelectorElementType
  extends IStubElementType<Angular2HtmlNgContentSelectorStub, Angular2HtmlNgContentSelector>
  implements ICompositeElementType {

  public Angular2HtmlNgContentSelectorElementType() {
    super("NG_CONTENT_SELECTOR", Angular2HtmlLanguage.INSTANCE);
  }

  @NonNls
  @Override
  public @NotNull String getExternalId() {
    return "NG-HTML:" + super.toString();
  }

  @Override
  public void serialize(@NotNull Angular2HtmlNgContentSelectorStub stub, @NotNull StubOutputStream dataStream) throws IOException {
    ((Angular2HtmlNgContentSelectorStubImpl)stub).serialize(dataStream);
  }

  @Override
  public @NotNull Angular2HtmlNgContentSelectorStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub)
    throws IOException {
    return new Angular2HtmlNgContentSelectorStubImpl(parentStub, dataStream);
  }

  @Override
  public void indexStub(@NotNull Angular2HtmlNgContentSelectorStub stub, @NotNull IndexSink sink) {
    ((Angular2HtmlNgContentSelectorStubImpl)stub).index(sink);
  }

  @Override
  public Angular2HtmlNgContentSelector createPsi(@NotNull Angular2HtmlNgContentSelectorStub stub) {
    return ((Angular2HtmlNgContentSelectorStubImpl)stub).createPsi();
  }

  @Override
  public @NotNull Angular2HtmlNgContentSelectorStub createStub(@NotNull Angular2HtmlNgContentSelector psi, StubElement parentStub) {
    return new Angular2HtmlNgContentSelectorStubImpl(psi, parentStub);
  }

  @Override
  public @NotNull ASTNode createCompositeNode() {
    return new CompositeElement(Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR);
  }

  public @NotNull Angular2HtmlNgContentSelector createPsi(ASTNode node) {
    return new Angular2HtmlNgContentSelectorImpl(node);
  }
}
