/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Condition

class LearnCondition : Condition<Any>, DumbAware {

  override fun value(o: Any): Boolean {
    return false
  }
}
