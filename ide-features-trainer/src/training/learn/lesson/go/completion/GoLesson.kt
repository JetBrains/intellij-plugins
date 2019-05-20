package training.learn.lesson.go.completion

import training.lang.GoLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson

abstract class GoLesson(name: String, module: Module) : KLesson(name, module, GoLangSupport.lang)
