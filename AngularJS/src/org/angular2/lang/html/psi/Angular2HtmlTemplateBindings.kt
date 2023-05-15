// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi

import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindings

/**
 * @see Angular2TemplateBindings
 *
 * @see Angular2TemplateBinding
 */
interface Angular2HtmlTemplateBindings : Angular2HtmlBoundAttribute {
  val templateName: String
  val bindings: Angular2TemplateBindings
}