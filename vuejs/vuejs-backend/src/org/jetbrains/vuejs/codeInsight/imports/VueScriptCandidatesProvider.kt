// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.imports

import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.JSImportCandidatesBase
import com.intellij.lang.javascript.modules.imports.providers.JSCandidatesProcessor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.model.source.DEFINE_EMITS_FUN
import org.jetbrains.vuejs.model.source.DEFINE_EXPOSE_FUN
import org.jetbrains.vuejs.model.source.DEFINE_MODEL_FUN
import org.jetbrains.vuejs.model.source.DEFINE_OPTIONS_FUN
import org.jetbrains.vuejs.model.source.DEFINE_PROPS_FUN
import org.jetbrains.vuejs.model.source.DEFINE_SLOTS_FUN
import org.jetbrains.vuejs.model.source.WITH_DEFAULTS_FUN
import java.util.function.Consumer

internal val SCRIPT_SETUP_API = setOf(
  DEFINE_PROPS_FUN,
  DEFINE_EMITS_FUN,
  DEFINE_EXPOSE_FUN,
  WITH_DEFAULTS_FUN,
  DEFINE_MODEL_FUN,
  DEFINE_OPTIONS_FUN,
  DEFINE_SLOTS_FUN,
)

class VueScriptCandidatesProvider(placeInfo: JSImportPlaceInfo) : JSImportCandidatesBase(placeInfo) {
  override fun collectNames(consumer: Consumer<String>) {
    SCRIPT_SETUP_API.forEach { consumer.accept(it) }
  }

  override fun processCandidates(name: String, processor: JSCandidatesProcessor) {
    if (SCRIPT_SETUP_API.contains(name)) {
      processor.remove(name)
    }
  }
}

class VueScriptCandidatesProviderFactory : JSImportCandidatesProvider.CandidatesFactory {
  override fun createProvider(placeInfo: JSImportPlaceInfo): JSImportCandidatesProvider? {
    return if (placeInfo.file.isVueFile) VueScriptCandidatesProvider(placeInfo) else null
  }
}