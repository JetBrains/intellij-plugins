// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg;

import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.tree.IStubFileElementType;
import org.angular2.lang.expr.parser.Angular2StubElementTypes;

public final class Angular2SvgFileElementType extends IStubFileElementType<PsiFileStub<HtmlFileImpl>> {

  public static final IStubFileElementType<PsiFileStub<HtmlFileImpl>> INSTANCE = new Angular2SvgFileElementType();

  private Angular2SvgFileElementType() {
    super(Angular2SvgLanguage.INSTANCE);
  }

  @Override
  public int getStubVersion() {
    return JSFileElementType.getVersion(Angular2StubElementTypes.STUB_VERSION);
  }
}
