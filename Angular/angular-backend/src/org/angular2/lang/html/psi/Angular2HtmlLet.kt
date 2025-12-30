// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi

import com.intellij.lang.javascript.psi.JSVariable

import com.intellij.psi.xml.XmlAttribute

interface Angular2HtmlLet : XmlAttribute {
  val variableName: String
  val variable: JSVariable?
}