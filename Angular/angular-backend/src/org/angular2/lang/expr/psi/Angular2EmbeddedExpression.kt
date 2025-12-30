// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSSourceElement
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.expr.psi.impl.Angular2BindingImpl

interface Angular2EmbeddedExpression : JSSourceElement, JSEmbeddedContent {

  val enclosingAttribute: XmlAttribute? get() = Angular2BindingImpl.getEnclosingAttribute(this)

}