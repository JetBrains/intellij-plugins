package com.intellij.dts.settings

import com.intellij.ide.util.BrowseFilesListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.util.whenDisposed
import com.intellij.openapi.observable.util.whenTextChanged
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.components.fields.ExtendableTextField
import java.awt.event.ActionEvent
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.JTextField
import javax.swing.plaf.basic.BasicComboBoxEditor

abstract class DtsSettingsPathInput<T>(private val disposable: Disposable?,
                                       private val browserTitle: @NlsContexts.DialogTitle String) : ComboBox<T>() {
  protected val textField = ExtendableTextField()

  var text: String by textField::text

  protected fun configure() {
    val editor = object : BasicComboBoxEditor() {
      override fun createEditorComponent(): JTextField {
        val listener = BrowseFilesListener(
          textField,
          FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle(browserTitle),
        )

        textField.addBrowseExtension({
                                       listener.actionPerformed(ActionEvent(textField, ActionEvent.ACTION_PERFORMED, "action"))
                                     }, disposable)

        return textField
      }

      override fun setItem(item: Any?) {
        // do not update text if item is deselected
        if (item == null) return
        super.setItem(item)
      }
    }

    isEditable = true
    setEditor(editor)

    textField.border = null
  }

  fun onTextChanged(callback: () -> Unit) {
    textField.document.whenTextChanged(disposable) { callback() }
  }

  fun onFocusLost(callback: () -> Unit) {
    val listener = object : FocusListener {
      override fun focusGained(e: FocusEvent?) {}
      override fun focusLost(e: FocusEvent?) = callback()
    }

    textField.addFocusListener(listener)

    disposable?.whenDisposed {
      textField.removeFocusListener(listener)
    }
  }
}
