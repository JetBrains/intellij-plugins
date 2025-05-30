// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr.parser;

import com.intellij.lang.javascript.psi.JSElementType;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSVariable;

public interface Angular2StubElementTypes {
  int STUB_VERSION = 6;

  String EXTERNAL_ID_PREFIX = "NG:";

  JSElementType<JSVariable> TEMPLATE_VARIABLE = new Angular2TemplateVariableElementType();

  JSElementType<JSVariable> BLOCK_PARAMETER_VARIABLE = new Angular2BlockParameterVariableElementType();

  JSElementType<JSLiteralExpression> STRING_PARTS_LITERAL_EXPRESSION =
    new Angular2StringPartsLiteralExpressionType();

  Angular2DeferredTimeLiteralExpressionElementType DEFERRED_TIME_LITERAL_EXPRESSION =
    new Angular2DeferredTimeLiteralExpressionElementType();
}
