// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.parser;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedAttributeElementType;
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedElementType;
import org.jetbrains.vuejs.lang.html.VueLanguage;
import org.jetbrains.vuejs.lang.html.psi.impl.VueRefAttributeImpl;
import org.jetbrains.vuejs.lang.html.psi.impl.VueRefAttributeStubImpl;

public interface VueStubElementTypes {

  int VERSION = 10;

  VueStubBasedTagElementType STUBBED_TAG = new VueStubBasedTagElementType("STUBBED_TAG");

  VueTemplateTagElementType TEMPLATE_TAG = new VueTemplateTagElementType();

  XmlStubBasedAttributeElementType STUBBED_ATTRIBUTE =
    new XmlStubBasedAttributeElementType("STUBBED_ATTRIBUTE", VueLanguage.Companion.getINSTANCE());

  VueScriptIdAttributeElementType SCRIPT_ID_ATTRIBUTE = new VueScriptIdAttributeElementType();

  VueSrcAttributeElementType SRC_ATTRIBUTE = new VueSrcAttributeElementType();

  XmlStubBasedElementType<VueRefAttributeStubImpl, VueRefAttributeImpl> REF_ATTRIBUTE = new VueRefAttributeElementType();

  VueScriptSetupEmbeddedContentElementType SCRIPT_SETUP_TS_EMBEDDED_CONTENT =
    new VueScriptSetupEmbeddedContentElementType(JavaScriptSupportLoader.TYPESCRIPT, "SCRIPT_SETUP_TS_");

  VueScriptSetupEmbeddedContentElementType SCRIPT_SETUP_JS_EMBEDDED_CONTENT =
    new VueScriptSetupEmbeddedContentElementType(JavaScriptSupportLoader.ECMA_SCRIPT_6, "SCRIPT_SETUP_JS_");
}
