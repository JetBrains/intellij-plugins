// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser;

import com.intellij.lang.javascript.types.JSParameterElementType;
import com.intellij.lang.javascript.types.JSVariableElementType;

public interface VueJSStubElementTypes {

  int STUB_VERSION = 1;

  String EXTERNAL_ID_PREFIX = "VUE-JS:";

  JSVariableElementType V_FOR_VARIABLE = new VueJSVForVariableElementType();

  JSParameterElementType SLOT_PROPS_PARAMETER = new VueJSSlotPropsParameterElementType();

  JSParameterElementType SCRIPT_SETUP_PARAMETER = new VueJSScriptSetupParameterElementType();

  VueJSScriptSetupTypeParameterListElementType SCRIPT_SETUP_TYPE_PARAMETER_LIST =
    new VueJSScriptSetupTypeParameterListElementType();
}

