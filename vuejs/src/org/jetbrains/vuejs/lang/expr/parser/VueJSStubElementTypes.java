// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser;

import com.intellij.lang.javascript.types.JSParameterElementType;
import com.intellij.lang.javascript.types.JSVariableElementType;
import org.jetbrains.vuejs.lang.expr.VueJSLanguage;
import org.jetbrains.vuejs.lang.expr.VueTSLanguage;

public interface VueJSStubElementTypes {

  int STUB_VERSION = 2;

  String EXTERNAL_ID_PREFIX = "VUE-JS:";

  JSVariableElementType V_FOR_VARIABLE = new VueJSVForVariableElementType();

  JSParameterElementType SLOT_PROPS_PARAMETER = new VueJSSlotPropsParameterElementType();

  JSParameterElementType SCRIPT_SETUP_PARAMETER = new VueJSScriptSetupParameterElementType();

  VueJSScriptSetupTypeParameterListElementType SCRIPT_SETUP_TYPE_PARAMETER_LIST =
    new VueJSScriptSetupTypeParameterListElementType();

  VueJSEmbeddedExpressionContentElementType EMBEDDED_EXPR_CONTENT_JS = new VueJSEmbeddedExpressionContentElementType(
    "EMBEDDED_EXPR_CONTENT_JS", VueJSLanguage.Companion.getINSTANCE());

  VueJSEmbeddedExpressionContentElementType EMBEDDED_EXPR_CONTENT_TS = new VueJSEmbeddedExpressionContentElementType(
    "EMBEDDED_EXPR_CONTENT_TS", VueTSLanguage.Companion.getINSTANCE());
}

