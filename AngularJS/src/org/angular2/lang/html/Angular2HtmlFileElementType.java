// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html;

import com.intellij.psi.tree.IStubFileElementType;

public class Angular2HtmlFileElementType extends IStubFileElementType {

  public static final IStubFileElementType INSTANCE = new Angular2HtmlFileElementType();

  private Angular2HtmlFileElementType() {
    super(Angular2HtmlLanguage.INSTANCE);
  }

  @Override
  public int getStubVersion() {
    return 2;
  }
}
