// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn

import org.jetbrains.annotations.Nls
import training.lang.LangSupport
import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.interfaces.ModuleType

class LearningModule(@Nls override val name: String,
                     @Nls override val description: String,
                     override val sanitizedName: String,
                     override val primaryLanguage: LangSupport,
                     override val moduleType: ModuleType,
                     initLessons: (LearningModule) -> List<Lesson>) : Module {

  override val lessons: List<Lesson> = initLessons(this)

  override fun toString(): String {
    return "($name for $primaryLanguage)"
  }

  override fun giveNotPassedLesson(): Lesson? {
    return lessons.firstOrNull { !it.passed }
  }

  override fun giveNotPassedAndNotOpenedLesson(): Lesson? {
    return lessons.firstOrNull { !it.passed }
  }

  override fun hasNotPassedLesson(): Boolean {
    return lessons.any { !it.passed }
  }
}
