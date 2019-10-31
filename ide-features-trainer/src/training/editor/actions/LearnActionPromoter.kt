/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.editor.actions

import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import java.util.*

class LearnActionPromoter : ActionPromoter {

  override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction> {
    for (action in actions) {
      if (action is LearnActions) {
        return ArrayList(actions)
      }
    }

    return ArrayList(actions)
  }
}
