// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.javascript.web.WebFrameworkDialect

interface Angular2HtmlDialect : WebFrameworkDialect {

  val templateSyntax: Angular2TemplateSyntax

  val svgDialect: Boolean

}