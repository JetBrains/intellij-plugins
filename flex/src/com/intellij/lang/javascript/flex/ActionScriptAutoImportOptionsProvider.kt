package com.intellij.lang.javascript.flex

import com.intellij.application.options.editor.AutoImportOptionsProvider
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationBundle
import com.intellij.openapi.options.DslConfigurableBase
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.layout.*

class ActionScriptAutoImportOptionsProvider : DslConfigurableBase(), AutoImportOptionsProvider {

  override fun createPanel(): DialogPanel {
    return panel {
      titledRow("ActionScript") {
        row {
          cell {
            checkBox(ApplicationBundle.message("checkbox.add.unambiguous.imports.on.the.fly"), ::addUnambiguousImportsOnTheFly)
            component(ContextHelpLabel.create(ApplicationBundle.message("help.add.unambiguous.imports")))
          }
        }
      }
    }
  }

  companion object {
    private const val ADD_IMPORTS_ON_THE_FLY_PROPERTY = "ActionScript.add.unambiguous.imports.on.the.fly"
    private const val ADD_IMPORTS_ON_THE_FLY_DEFAULT = false

    @JvmStatic
    var addUnambiguousImportsOnTheFly: Boolean
      @JvmName("isAddUnambiguousImportsOnTheFly")
      get() = PropertiesComponent.getInstance().getBoolean(ADD_IMPORTS_ON_THE_FLY_PROPERTY, ADD_IMPORTS_ON_THE_FLY_DEFAULT)
      set(value) {
        PropertiesComponent.getInstance().setValue(ADD_IMPORTS_ON_THE_FLY_PROPERTY, value)
      }

  }
}