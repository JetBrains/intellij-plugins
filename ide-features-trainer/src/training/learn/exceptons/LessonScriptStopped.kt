/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.exceptons

/** This exception is used to indicate that lesson is interrupted by another lesson or by some other reason */
class LessonScriptStopped : Exception() {
}