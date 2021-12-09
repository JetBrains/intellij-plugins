package com.intellij.lang.javascript.flex

import com.intellij.application.options.editor.AutoImportOptionsProvider
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationBundle
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.bindSelected

class ActionScriptAutoImportOptionsProvider : UiDslUnnamedConfigurable.Simple(), AutoImportOptionsProvider {

  override fun Panel.createContent() {
    group("ActionScript") {
      row {
        checkBox(ApplicationBundle.message("checkbox.add.unambiguous.imports.on.the.fly"))
          .bindSelected(::addUnambiguousImportsOnTheFly)
          .gap(RightGap.SMALL)
        contextHelp(ApplicationBundle.message("help.add.unambiguous.imports"))
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