// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.lesson

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.application.ModalityState
import training.dsl.LessonContext

fun LessonContext.setLanguageLevel() {
  prepareRuntimeTask(ModalityState.nonModal()) {
    JSRootConfiguration.getInstance(project).storeLanguageLevelAndUpdateCaches(JSLanguageLevel.ES6)
  }
}
