/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.interfaces

interface Module {

  val lessons: List<Lesson>

  val sanitizedName: String

  var id: String?

  val name: String

  val primaryLanguage: String?

  val moduleType: ModuleType

  val description: String?

  fun giveNotPassedLesson(): Lesson?

  fun giveNotPassedAndNotOpenedLesson(): Lesson?

  fun hasNotPassedLesson(): Boolean
}