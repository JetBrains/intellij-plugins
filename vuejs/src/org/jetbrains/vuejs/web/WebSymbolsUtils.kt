// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.codeInsight.html.elements.WebSymbolElementDescriptor
import org.jetbrains.vuejs.model.VueModelDirectiveProperties
import org.jetbrains.vuejs.model.VueModelDirectiveProperties.Companion.DEFAULT_EVENT
import org.jetbrains.vuejs.model.VueModelDirectiveProperties.Companion.DEFAULT_PROP
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider.Companion.KIND_VUE_MODEL
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider.Companion.PROP_VUE_MODEL_EVENT
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider.Companion.PROP_VUE_MODEL_PROP

fun WebSymbolElementDescriptor.getModel(): VueModelDirectiveProperties =
  runNameMatchQuery(listOf(KIND_VUE_MODEL)).firstOrNull()
    ?.let {
      VueModelDirectiveProperties(prop = it.properties[PROP_VUE_MODEL_PROP] as? String ?: DEFAULT_PROP,
                                  event = it.properties[PROP_VUE_MODEL_EVENT] as? String ?: DEFAULT_EVENT)
    }
  ?: VueModelDirectiveProperties()