package org.jetbrains.qodana.ui.run

import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.panels.Wrapper
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.editorViewComponentFromFlow
import java.awt.Component
import javax.swing.text.JTextComponent

fun qodanaYamlView(scope: CoroutineScope, viewModel: QodanaYamlViewModel): Wrapper {
  val editorWrapper = editorViewComponentFromFlow(scope, viewModel.yamlStateFlow.mapNotNull { it?.editor }, viewModel.project)
  scope.launch(QodanaDispatchers.Ui) {
    launch {
      val validatorDisposable = Disposer.newDisposable()
      try {
        val validator = ComponentValidator(validatorDisposable).installOn(editorWrapper)
        viewModel.yamlValidationErrorFlow.distinctUntilChanged().collect {
          validator.updateInfo(it?.let { ValidationInfo(it.message, editorWrapper) })
          focusableTextComponentIn(editorWrapper)?.requestFocus()
        }
      }
      finally {
        Disposer.dispose(validatorDisposable)
      }
    }
  }
  return editorWrapper
}

private fun focusableTextComponentIn(parent: Component): Component? {
  return UIUtil.uiTraverser(parent).firstOrNull { it is JTextComponent && it.isFocusable }
}