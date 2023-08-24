package com.intellij.dts.settings

import com.intellij.dts.DtsBundle
import com.intellij.dts.util.Either
import com.intellij.dts.zephyr.DtsZephyrProvider
import com.intellij.ide.util.BrowseFilesListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.util.whenTextChanged
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.dsl.builder.*
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.awt.event.ActionEvent
import java.nio.file.Path
import javax.swing.JTextField
import javax.swing.plaf.basic.BasicComboBoxEditor
import kotlin.io.path.exists

class DtsSettingsConfigurable(private val project: Project) : BoundConfigurable(DtsBundle.message("settings.name")) {
    private val state: DtsSettings.State = DtsSettings.of(project).state

    private fun bundle(key: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String, suffix: @NlsSafe String = ""): @Nls String {
        return DtsBundle.message(key) + suffix
    }

    private fun validateRoot(path: String): Result<String> {
        val provider = DtsZephyrProvider.of(project)

        if (path.isBlank()) {
            val root = provider.searchRoot()
                ?: return Either.Left(DtsBundle.message("settings.zephyr.root.not_found"))

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
        val boardArchInput = JBTextField()
        val boardNameInput = JBTextField()

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

            override fun evaluate(state: String, result: Result<String>) {
                if (!state.isBlank()) return

                result.fold({
                    rootInput.setEmptyText(DtsBundle.message("settings.zephyr.root.detecting.failed"))
                }, {
                    rootInput.setEmptyText(DtsBundle.message("settings.zephyr.root.detecting.success", it))
                })
            }
        }

        val boardStatus = object : DtsSettingsInputStatus<BoardState, Unit>(disposable) {
            override fun readState(): BoardState {
                return BoardState(
                    root = rootInput.text,
                    arch = boardArchInput.text,
                    name = boardNameInput.text,
                )
            }

            override fun performCheck(state: BoardState): Result<Unit> {
                val root = validateRoot(state.root).fold({ cancel("settings.zephyr.board.no_root") }, { it })

                if (state.name.isBlank()) return error("settings.zephyr.board.name.empty")
                if (state.arch.isBlank()) return error("settings.zephyr.board.arch.empty")

                return if (!Path.of(root, "boards", state.arch, state.name).exists()) {
                    error("settings.zephyr.board.not_found")
                } else {
                    success(Unit)
                }
            }
        }

        group(bundle("settings.zephyr.group")) {
            row(bundle("settings.zephyr.root", ":")) {
                cell(rootInput).columns(COLUMNS_LARGE).bind(
                    { input -> input.text },
                    { input, value -> input.text = value },
                    state::zephyrRoot.toMutableProperty()
                )
            }
            row("") {
                cell(rootStatus)
            }
            row(bundle("settings.zephyr.board.arch", ":")) {
                cell(boardArchInput).columns(COLUMNS_MEDIUM).bindText(state::zephyrArch)
            }
            row(bundle("settings.zephyr.board.name", ":")) {
                cell(boardNameInput).columns(COLUMNS_MEDIUM).bindText(state::zephyrBoard)
            }
            row("") {
                cell(boardStatus)
            }
        }

        rootInput.onTextChanged(rootStatus::check)
        rootInput.onTextChanged(boardStatus::check)

        boardNameInput.whenTextChanged(disposable) { boardStatus.check() }
        boardArchInput.whenTextChanged(disposable) { boardStatus.check() }

        rootStatus.check()
        boardStatus.check()
    }

    override fun apply() {
        DtsSettings.of(project).update { super.apply() }
    }
}

private data class BoardState(val root: String, val arch: String, val name: String)

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
}