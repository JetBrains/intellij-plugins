/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.util

object XmlModuleConstants {
  const val MODULE_ALLMODULE_FILENAME = "modules.xml"
  const val MODULE_TYPE_ATTR = "module"
  const val LANGUAGE_NODE_ATTR = "language"
  const val LANGUAGE_NAME_ATTR = "lang"
  const val MODULE_NAME_ATTR = "name"
  const val MODULE_ID_ATTR = "id"
  const val MODULE_XML_LESSON_ELEMENT = "lesson"
  const val MODULE_KT_LESSON_ELEMENT = "lesson-kt" // Kotlin-described lesson

  const val MODULE_ANSWER_PATH_ATTR = "answerPath"
  const val MODULE_DESCRIPTION_ATTR = "description"
  const val MODULE_SDK_TYPE = "sdkType"
  const val MODULE_FILE_TYPE = "fileType"
  /**
   * Some lessons could be unfinished or badly implemented and does not fit for release.
   * Nevertheless they are accessible from master snapshots.
   */
  const val MODULE_LESSON_UNFINISHED_ATTR = "unfinished"
  const val MODULE_LESSONS_PATH_ATTR = "lessonsPath"
  const val MODULE_LESSON_FILENAME_ATTR = "filename"
  const val MODULE_LESSON_IMPLEMENTATION_ATTR = "implementationClass"
  const val MODULE_LESSON_LANGUAGE_ATTR = "lang"
  const val MODULE_LESSON_SAMPLE_ATTR = "sample"
}