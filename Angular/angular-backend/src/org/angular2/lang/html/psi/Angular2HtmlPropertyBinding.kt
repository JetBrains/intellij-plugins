// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi

import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2Interpolation

interface Angular2HtmlPropertyBinding : Angular2HtmlBoundAttribute {
  val propertyName: String
  val binding: Angular2Binding?
  val interpolations: Array<Angular2Interpolation>
  val bindingType: PropertyBindingType
}