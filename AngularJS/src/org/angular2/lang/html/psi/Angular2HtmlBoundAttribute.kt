// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi

import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.html.parser.Angular2AttributeNameParser

interface Angular2HtmlBoundAttribute : XmlAttribute {
  val attributeInfo: Angular2AttributeNameParser.AttributeInfo
}