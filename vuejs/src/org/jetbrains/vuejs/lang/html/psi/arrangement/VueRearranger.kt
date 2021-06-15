// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.arrangement

import com.intellij.psi.codeStyle.arrangement.ArrangementSettingsSerializer
import com.intellij.psi.codeStyle.arrangement.DefaultArrangementSettingsSerializer
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementEntryMatcher
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule
import com.intellij.psi.codeStyle.arrangement.model.ArrangementAtomMatchCondition
import com.intellij.psi.codeStyle.arrangement.model.ArrangementCompositeMatchCondition
import com.intellij.psi.codeStyle.arrangement.std.ArrangementStandardSettingsAware.ArrangementTabInfo
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens
import com.intellij.xml.arrangement.HtmlRearranger
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueRearranger : HtmlRearranger() {


  override fun getDefaultSettings(): StdArrangementSettings? {
    return DEFAULT_SETTINGS
  }

  override fun getSerializer(): ArrangementSettingsSerializer {
    return SETTINGS_SERIALIZER
  }

  override fun getArrangementTabInfos(): Collection<ArrangementTabInfo> {
    val displayName = VueLanguage.INSTANCE.displayName
    return listOf(ArrangementTabInfo(VuejsIcons.Vue, displayName, displayName))
  }

  companion object {

    val DEFAULT_SETTINGS: StdArrangementSettings = StdArrangementSettings.createByMatchRules(
      emptyList(), VueAttributeKind.values().map { it.createRule() })

    private val SETTINGS_SERIALIZER = DefaultArrangementSettingsSerializer(DEFAULT_SETTINGS)
  }

  enum class VueAttributeKind(val pattern: String) {
    DEFINITION("(v-bind:|:|v-)?is"),
    LIST_RENDERING("v-for"),
    CONDITIONALS("v-(if|else-if|else|show|cloak)"),
    RENDER_MODIFIERS("v-(pre|once)"),
    GLOBAL("(v-bind:|:)?id"),
    UNIQUE("(v-bind:|:)?(ref|key|slot|slot-scope)|v-slot"),
    TWO_WAY_BINDING("v-model"),
    OTHER_DIRECTIVES("v-(?!on:|bind:|(html|text)$).+"),
    OTHER_ATTR("(?!v-on:|@|v-html$|v-text$).+"),
    EVENTS("(v-on:|@)\\w+"),
    CONTENT("v-html|v-text");

    fun createRule(): StdArrangementMatchRule {
      return StdArrangementMatchRule(StdArrangementEntryMatcher(ArrangementCompositeMatchCondition(
        listOf(
          ArrangementAtomMatchCondition(StdArrangementTokens.EntryType.XML_ATTRIBUTE),
          ArrangementAtomMatchCondition(StdArrangementTokens.Regexp.NAME, pattern))
      )), StdArrangementTokens.Order.BY_NAME)
    }

  }

}
