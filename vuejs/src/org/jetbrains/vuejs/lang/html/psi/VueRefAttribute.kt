// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi

import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.pom.PomTarget
import com.intellij.psi.xml.XmlAttribute

interface VueRefAttribute : XmlAttribute {

  val isList: Boolean

  val containingTagName: String

  val implicitElement: JSImplicitElement?

  interface VueRefDeclaration : PomTarget, JSImplicitElement, JSTypeOwner

}