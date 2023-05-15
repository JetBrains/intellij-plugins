// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.daemon.impl.analysis.XmlUnboundNsPrefixInspection
import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.htmlInspections.*
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedAttributeInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlDeprecatedTagInspection
import com.intellij.lang.javascript.inspections.*
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import com.intellij.xml.util.CheckEmptyTagInspection
import com.sixrr.inspectjs.validity.ThisExpressionReferencesGlobalObjectJSInspection
import org.jetbrains.vuejs.inspections.*

class VueInspectionsProvider : InspectionToolProvider {
  override fun getInspectionClasses(): Array<Class<out LocalInspectionTool>> =
    arrayOf(
      HtmlDeprecatedAttributeInspection::class.java,
      HtmlDeprecatedTagInspection::class.java,
      HtmlExtraClosingTagInspection::class.java,
      HtmlUnknownBooleanAttributeInspection::class.java,
      HtmlUnknownAttributeInspection::class.java,
      HtmlUnknownTagInspection::class.java,
      HtmlWrongAttributeValueInspection::class.java,
      VueDeprecatedSymbolInspection::class.java,
      VueUnrecognizedDirectiveInspection::class.java,
      VueUnrecognizedSlotInspection::class.java,
      VueMissingComponentImportInspection::class.java,
      RequiredAttributesInspection::class.java,
      DuplicateTagInspection::class.java,
      XmlUnboundNsPrefixInspection::class.java,
      CheckEmptyTagInspection::class.java,
      JSAnnotatorInspection::class.java,
      JSCheckFunctionSignaturesInspection::class.java,
      JSConstantReassignmentInspection::class.java,
      JSUnresolvedReferenceInspection::class.java,
      JSUndeclaredVariableInspection::class.java,
      JSUnusedLocalSymbolsInspection::class.java,
      JSValidateTypesInspection::class.java,
      JSIncompatibleTypesComparisonInspection::class.java,
      ThisExpressionReferencesGlobalObjectJSInspection::class.java,
      TypeScriptValidateTypesInspection::class.java,
      TypeScriptUnresolvedReferenceInspection::class.java,
    )
}