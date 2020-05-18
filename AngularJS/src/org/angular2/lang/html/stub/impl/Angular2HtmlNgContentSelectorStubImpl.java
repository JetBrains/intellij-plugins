// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub.impl;

import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector;
import org.angular2.lang.html.psi.impl.Angular2HtmlNgContentSelectorImpl;
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorStub;
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Angular2HtmlNgContentSelectorStubImpl extends StubBase<Angular2HtmlNgContentSelector>
  implements Angular2HtmlNgContentSelectorStub {

  private final StringRef mySelector;

  public Angular2HtmlNgContentSelectorStubImpl(StubElement parent,
                                               @NotNull StubInputStream dataStream) throws IOException {
    super(parent, Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR);
    mySelector = dataStream.readName();
  }

  public Angular2HtmlNgContentSelectorStubImpl(Angular2HtmlNgContentSelector psi,
                                               StubElement parent) {
    super(parent, Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR);
    mySelector = StringRef.fromString(psi.getText());
  }

  public void serialize(StubOutputStream stream) throws IOException {
    stream.writeName(StringRef.toString(mySelector));
  }

  public void index(IndexSink sink) {
  }

  public Angular2HtmlNgContentSelector createPsi() {
    return new Angular2HtmlNgContentSelectorImpl(this, Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR);
  }

  @Override
  public @Nullable String getSelector() {
    return StringRef.toString(mySelector);
  }
}
