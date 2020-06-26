// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html;

import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.xml.HtmlFileElementType;
import org.angular2.lang.expr.parser.Angular2StubElementTypes;

public final class Angular2HtmlFileElementType extends IStubFileElementType<PsiFileStub<HtmlFileImpl>> {

  public static final IStubFileElementType<PsiFileStub<HtmlFileImpl>> INSTANCE = new Angular2HtmlFileElementType();

  private Angular2HtmlFileElementType() {
    super(Angular2HtmlLanguage.INSTANCE);
  }

  @Override
  public int getStubVersion() {
    return HtmlFileElementType.getHtmlStubVersion() + Angular2StubElementTypes.STUB_VERSION;
  }
}
