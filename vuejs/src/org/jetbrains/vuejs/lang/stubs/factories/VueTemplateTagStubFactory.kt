// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.factories

import com.intellij.psi.impl.source.xml.stub.XmlTagStubImpl
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.TEMPLATE_TAG
import org.jetbrains.vuejs.lang.html.psi.impl.VueTemplateTagImpl

class VueTemplateTagStubFactory : VueStubBasedTagStubFactory(TEMPLATE_TAG) {
  override fun createPsi(stub: XmlTagStubImpl) = VueTemplateTagImpl(stub, elementType)
}