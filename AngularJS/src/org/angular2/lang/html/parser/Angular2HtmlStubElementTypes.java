// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser;

import com.intellij.lang.javascript.psi.JSStubElementType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorElementType;

public interface Angular2HtmlStubElementTypes {
  JSStubElementType<JSVariableStub<JSVariable>, JSVariable> REFERENCE_VARIABLE = new Angular2HtmlReferenceVariableElementType();

  Angular2HtmlNgContentSelectorElementType NG_CONTENT_SELECTOR = new Angular2HtmlNgContentSelectorElementType();
}
