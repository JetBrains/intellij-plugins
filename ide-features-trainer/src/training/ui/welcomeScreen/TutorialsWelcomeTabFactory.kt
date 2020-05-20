// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen

import com.intellij.openapi.Disposable
import com.intellij.openapi.wm.WelcomeScreenTab
import com.intellij.openapi.wm.WelcomeTabFactory
import com.intellij.openapi.wm.impl.welcomeScreen.TabbedWelcomeScreen.DefaultWelcomeScreenTab
import training.ui.views.ModulesPanel
import javax.swing.JComponent

class TutorialsWelcomeTabFactory : WelcomeTabFactory {
  override fun createWelcomeTab(parentDisposable: Disposable): WelcomeScreenTab {
    return object : DefaultWelcomeScreenTab("Tutorials") {
      override fun buildComponent(): JComponent {
        return ModulesPanel(null)
      }
    }
  }
}