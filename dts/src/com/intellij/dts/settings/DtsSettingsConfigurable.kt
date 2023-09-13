package com.intellij.dts.settings

import com.intellij.dts.DtsBundle
import com.intellij.dts.util.Either
import com.intellij.dts.zephyr.DtsZephyrProvider
import com.intellij.ide.util.BrowseFilesListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.util.whenDisposed
import com.intellij.openapi.observable.util.whenTextChanged
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.dsl.builder.*
import org.jetbrains.annotations.Nls
import java.awt.event.ActionEvent
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.nio.file.Path
import javax.swing.JTextField
import javax.swing.plaf.basic.BasicComboBoxEditor
import kotlin.io.path.*

class DtsSettingsConfigurable(private val project: Project) : BoundConfigurable(DtsBundle.message("settings.name")) {
    private val state: DtsSettings.State = DtsSettings.of(project).state

    private fun getBoardPath(): String? {
        if (state.zephyrArch.isBlank() || state.zephyrBoard.isBlank()) return null

        val path = Path.of(state.zephyrArch, state.zephyrBoard)
        return path.pathString
    }

    private fun setBoardPath(pathStr: String?) {
        if (pathStr == null) return
        val path = Path.of(pathStr)

        state.zephyrArch = path.parent.name
        state.zephyrBoard = path.name
    }

    private fun validateRoot(path: String): Result<String> {
        val provider = DtsZephyrProvider.of(project)

        if (path.isBlank()) {
            val root = provider.searchRoot() ?: return Either.Left(DtsBundle.message("settings.zephyr.root.not_found"))

            return Either.Right(root.path)
        } else {
            val root = LocalFileSystem.getInstance().findFileByNioFile(Path.of(path))
                ?: return Either.Left(DtsBundle.message("settings.zephyr.root.not_found"))

            if (!provider.validateRoot(root)) {
                return Either.Left(DtsBundle.message("settings.zephyr.root.invalid"))
            }

            return Either.Right(path)
        }
    }

    override fun createPanel(): DialogPanel = panel {
        val rootInput = RootComboBox(disposable)

        val boardInput = ComboBox<String>()
        val boardModel = CollectionComboBoxModel<String>(mutableListOf(), getBoardPath())

        boardInput.renderer = SimpleListCellRenderer.create(DtsBundle.message("settings.zephyr.board.empty")) { it }
        boardInput.model = boardModel
        boardInput.isSwingPopup = false

        val rootStatus = object : DtsSettingsInputStatus<String, String>(disposable) {
            override fun readState(): String {
                val state = rootInput.text

                if (state.isBlank()) {
                    rootInput.setEmptyText(DtsBundle.message("settings.zephyr.root.detecting.ongoing"))
                } else {
                    rootInput.setEmptyText("")
                }

                return state
            }

            override fun performCheck(state: String): Result<String> = validateRoot(state)

            override fun evaluate(state: String, result: Result<String>): ValidationInfo? {
                if (state.isBlank()) {
                    result.fold({
                        rootInput.setEmptyText(DtsBundle.message("settings.zephyr.root.detecting.failed"))
                    }, {
                        rootInput.setEmptyText(DtsBundle.message("settings.zephyr.root.detecting.success", it))
                    })
                }

                return super.evaluate(state, result)
            }
        }

        val boardStatus = object : DtsSettingsInputStatus<String, List<String>>(disposable) {
            override fun readState(): String = rootInput.text

            override fun performCheck(state: String): Result<List<String>> = validateRoot(state).mapRight { root ->
                val results = mutableListOf<String>()

                val boards = Path.of(root, "boards")
                if (!boards.isDirectory()) return@mapRight results

                for (arch in boards.listDirectoryEntries()) {
                    if (!arch.isDirectory() || arch.name == "common") continue

                    for (board in arch.listDirectoryEntries()) {
                        if (!arch.isDirectory() || !board.resolve("board.cmake").exists()) continue

                        results.add(boards.relativize(board).pathString)
                    }
                }

                results
            }

            override fun evaluate(state: String, result: Result<List<String>>): ValidationInfo? {
                result.fold({
                    boardInput.isEnabled = false
                    boardModel.removeAll()
                }, {
                    boardInput.isEnabled = true
                    boardModel.replaceAll(it)
                })

                return super.evaluate(state, result)
            }
        }

        group(DtsBundle.message("settings.zephyr.group")) {
            row(DtsBundle.message("settings.zephyr.root") + ":") {
                cell(rootInput).columns(COLUMNS_LARGE).bind(
                    { input -> input.text },
                    { input, value -> input.text = value },
                    state::zephyrRoot.toMutableProperty(),
                )
            }
            row(DtsBundle.message("settings.zephyr.board") + ":") {
                cell(boardInput).columns(COLUMNS_MEDIUM).bindItem(
                    ::getBoardPath,
                    ::setBoardPath,
                )
            }
        }

        rootStatus.installOn(rootInput)

        rootInput.onFocusLost(rootStatus::check)
        rootInput.onTextChanged(boardStatus::check)

        rootStatus.check()
        boardStatus.check()
    }

    override fun apply() {
        DtsSettings.of(project).update { super.apply() }
    }
}

private class RootComboBox(private val disposable: Disposable?) : ComboBox<Any>() {
    private val textField = ExtendableTextField()

    var text: String by textField::text

    init {
        val editor = object : BasicComboBoxEditor() {
            override fun createEditorComponent(): JTextField {
                val listener = BrowseFilesListener(
                    textField,
                    DtsBundle.message("settings.zephyr.root.browse"),
                    null,
                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                )

                textField.addBrowseExtension({
                    listener.actionPerformed(ActionEvent(textField, ActionEvent.ACTION_PERFORMED, "action"))
                }, disposable)

                return textField
            }
        }

        isEditable = true
        setEditor(editor)

        model = CollectionComboBoxModel(listOf(""))
        renderer = SimpleListCellRenderer.create("") { DtsBundle.message("settings.zephyr.root.auto_detect") }

        textField.border = null
    }

    fun onTextChanged(callback: () -> Unit) {
        textField.document.whenTextChanged(disposable) { callback() }
    }

    fun setEmptyText(text: @Nls String) {
        textField.emptyText.text = text
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