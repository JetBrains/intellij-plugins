// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

enum class Angular2TemplateSyntax(val tokenizeExpansionForms: Boolean,
                                  val enableBlockSyntax: Boolean) {
  V_2(true, false),
  V_2_NO_EXPANSION_FORMS(false, false),
  V_17(true, true),
}