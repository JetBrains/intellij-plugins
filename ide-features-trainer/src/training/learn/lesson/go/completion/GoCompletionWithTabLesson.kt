/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson.go.completion

import org.intellij.lang.annotations.Language
import training.lang.GoLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.general.CompletionWithTabLesson
import training.learn.lesson.kimpl.parseLessonSample

class GoCompletionWithTabLesson(module: Module) : CompletionWithTabLesson(module, GoLangSupport.lang, "Errorf") {

  @Language("go")
  override val sample = parseLessonSample("""package main

import (
	"fmt"
)

func main() {
	fmt.<caret>Printf("hello world")
}
""")

}