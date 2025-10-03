// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight

/**
 * TODO WEB-74979 I started moving code from
 * contrib/vuejs/src/org/jetbrains/vuejs/codeInsight/VueUtil.kt
 * Later it can be either moved in a single commit not to lose git history,
 * or restructured.
 */

const val SETUP_ATTRIBUTE_NAME: String = "setup"
const val VAPOR_ATTRIBUTE_NAME: String = "vapor"
const val REF_ATTRIBUTE_NAME: String = "ref"
const val MODULE_ATTRIBUTE_NAME: String = "module"
const val GENERIC_ATTRIBUTE_NAME: String = "generic"
const val ATTR_DIRECTIVE_PREFIX: String = "v-"
const val ATTR_EVENT_SHORTHAND: Char = '@'
const val ATTR_SLOT_SHORTHAND: Char = '#'
const val ATTR_ARGUMENT_PREFIX: Char = ':'
const val ATTR_MODIFIER_PREFIX: Char = '.'

const val FUNCTIONAL_COMPONENT_TYPE: String = "FunctionalComponent"

const val VITE_PKG: String = "vite"