// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.psi

import com.intellij.psi.xml.XmlElement
import org.angular2.lang.expr.psi.Angular2BlockParameter

interface Angular2HtmlBlockParameters : XmlElement {

  val parameters: List<Angular2BlockParameter>

}