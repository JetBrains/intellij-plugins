// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser;

import com.intellij.lang.javascript.psi.JSStubElementType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;

public interface Angular2StubElementTypes {
  int STUB_VERSION = 4;

  String EXTERNAL_ID_PREFIX = "NG:";

  JSStubElementType<JSVariableStub<JSVariable>, JSVariable> TEMPLATE_VARIABLE = new Angular2TemplateVariableElementType();
}
