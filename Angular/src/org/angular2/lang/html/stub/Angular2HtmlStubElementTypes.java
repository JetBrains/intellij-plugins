// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.stub;

import com.intellij.lang.javascript.psi.JSStubElementType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;

import static org.angular2.lang.html.psi.Angular2HtmlAttrVariable.Kind.LET;
import static org.angular2.lang.html.psi.Angular2HtmlAttrVariable.Kind.REFERENCE;

public interface Angular2HtmlStubElementTypes {

  int STUB_VERSION = 1;

  String EXTERNAL_ID_PREFIX = "NG-HTML:";

  JSStubElementType<JSVariableStub<JSVariable>, JSVariable> REFERENCE_VARIABLE = new Angular2HtmlVariableElementType(REFERENCE);
  JSStubElementType<JSVariableStub<JSVariable>, JSVariable> LET_VARIABLE = new Angular2HtmlVariableElementType(LET);

  Angular2HtmlNgContentSelectorElementType NG_CONTENT_SELECTOR = new Angular2HtmlNgContentSelectorElementType();
}

