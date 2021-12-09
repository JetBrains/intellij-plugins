// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring.extractComponent

import com.intellij.lang.ecmascript6.refactoring.ES6ReferenceExpressionsInfo
import com.intellij.lang.javascript.psi.JSType
import com.intellij.openapi.diagnostic.ControlFlowException
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.TextRange
import org.angular2.lang.html.parser.Angular2AttributeType

class Angular2ExtractComponentUnsupportedException(@NlsContexts.DialogMessage message: String) : Exception(message), ControlFlowException

data class Angular2ExtractedComponent(
  val template: String,
  val sourceStartOffset: Int,
  val attributes: List<Attr>,
  val replacements: List<Replacement>,
  val importedInfos: List<ES6ReferenceExpressionsInfo>
)

data class Attr(
  val name: String,
  val jsType: JSType,
  val assignedValue: String,
  val attributeType: Angular2AttributeType,
)

data class Replacement(val textRange: TextRange, val text: String)