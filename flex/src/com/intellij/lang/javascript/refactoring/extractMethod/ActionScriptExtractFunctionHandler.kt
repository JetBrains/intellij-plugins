// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.refactoring.extractMethod

import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.text.StringUtil

internal class ActionScriptExtractFunctionHandler : JSExtractFunctionHandler() {
  override fun showDialog(
    ci: ContextInfo,
    signatureGenerator: ExtractedFunctionSignatureGenerator,
    scope: IntroductionScope,
    occurrences: Array<out JSExpression>,
  ): JSExtractFunctionSettings? {
    val dialog = ActionScriptExtractFunctionDialog(signatureGenerator, ci, scope, occurrences)
    dialog.show()

    if (dialog.exitCode != DialogWrapper.OK_EXIT_CODE) {
      return null
    }

    return dialog
  }

  companion object {
    private const val DECLARE_STATIC_PROPERTY_KEY = "js.extract.function.declare.static"
    private const val CLASS_MEMBER_VISIBILITY_PROPERTY_KEY = "js.extract.function.last.visibility"

    @JvmStatic
    fun getDeclareStatic(): Boolean {
      return PropertiesComponent.getInstance().getBoolean(DECLARE_STATIC_PROPERTY_KEY)
    }

    @JvmStatic
    fun saveDeclareStatic(value: Boolean) {
      PropertiesComponent.getInstance().setValue(DECLARE_STATIC_PROPERTY_KEY, value)
    }

    @JvmStatic
    fun getClassMemberVisibility(): JSAttributeList.AccessType? {
      val value = PropertiesComponent.getInstance().getValue(CLASS_MEMBER_VISIBILITY_PROPERTY_KEY)
                  ?: return null

      return StringUtil.parseEnum(value, null, JSAttributeList.AccessType::class.java)
    }

    @JvmStatic
    fun saveClassMemberVisibility(accessType: JSAttributeList.AccessType?) {
      val propertiesComponent = PropertiesComponent.getInstance()
      if (accessType == null) {
        propertiesComponent.unsetValue(CLASS_MEMBER_VISIBILITY_PROPERTY_KEY)
      }
      else {
        propertiesComponent.setValue(CLASS_MEMBER_VISIBILITY_PROPERTY_KEY, accessType.toString())
      }
    }
  }
}
