/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson.go.completion

import training.lang.GoLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson

abstract class GoLesson(name: String, module: Module) : KLesson(name, module, GoLangSupport.lang)
