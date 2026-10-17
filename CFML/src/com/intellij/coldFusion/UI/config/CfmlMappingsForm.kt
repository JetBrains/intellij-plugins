// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.config

import com.intellij.coldFusion.CfmlBundle
import com.intellij.coldFusion.model.CfmlLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.intellij.util.ui.AbstractTableCellEditor
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.LocalPathCellEditor
import com.intellij.util.ui.ValidatingTableEditor
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.table.TableCellEditor

internal class CfmlMappingsForm(private val project: Project) {

  private lateinit var languageLevel: ComboBox<String>
  private val tableEditor = object : ValidatingTableEditor<Item>() {
    override fun cloneOf(item: Item): Item {
      return Item(item.directoryPath, item.logicalPath)
    }

    override fun validate(item: Item): String? {
      return when {
        item.directoryPath.isEmpty() -> CfmlBundle.message("directory.path.is.empty")
        item.logicalPath.isEmpty() -> CfmlBundle.message("logical.path.is.empty")
        !item.logicalPath.startsWith("/") && !item.logicalPath.startsWith("\\") -> CfmlBundle.message("incorrect.logical.path")
        else -> {
          val file = StandardFileSystems.local().findFileByPath(item.directoryPath)
          if (file == null || !file.isValid || !file.isDirectory)
            CfmlBundle.message("directory.path.is.not.found", item.directoryPath)
          else null
        }
      }
    }

    override fun createItem(): Item {
      return Item("", "")
    }
  }

  init {
    tableEditor.setShowGrid(false)
    tableEditor.emptyText.text = CfmlBundle.message("no.mapping")
  }

  @JvmField
  val panel = panel {
    row(CfmlBundle.message("mappings.form.lang.version")) {
      languageLevel = comboBox(
        listOf(CfmlLanguage.CF8, CfmlLanguage.CF9, CfmlLanguage.CF10, CfmlLanguage.CF11, CfmlLanguage.RAILO, CfmlLanguage.LUCEE),
        textListCellRenderer("") {
          when (it) {
            CfmlLanguage.CF8 -> "ColdFusion 8"
            CfmlLanguage.CF9 -> "ColdFusion 9"
            CfmlLanguage.CF10 -> "ColdFusion 10"
            CfmlLanguage.CF11 -> "ColdFusion 11"
            CfmlLanguage.RAILO -> "Railo"
            CfmlLanguage.LUCEE -> "Lucee"
            else -> ""
          }
        })
        .component
    }
    row {
      cell(tableEditor.contentPane as JComponent)
        .label(CfmlBundle.message("border.text.server.mappings"), LabelPosition.TOP)
        .align(Align.FILL)
    }.resizableRow()
  }

  private class Item(directoryPath: String, var logicalPath: String) {
    var directoryPath: String = FileUtil.toSystemDependentName(directoryPath)

    fun isEmpty(): Boolean {
      return directoryPath.isEmpty() && logicalPath.isEmpty()
    }
  }

  private fun setItems(paths: Map<String?, String>) {
    val items = paths.map { (logicalPath, directoryPath) -> Item(FileUtil.toSystemDependentName(directoryPath), logicalPath ?: "") }

    @Suppress("UNCHECKED_CAST")
    val columns = arrayOf<ColumnInfo<Item, *>>(DirectoryPathColumnInfo(), LogicalPathColumnInfo()) as Array<ColumnInfo<Item, Any>>
    tableEditor.setModel(columns, items)
  }

  private fun getPaths(): Map<String, String> {
    val result = HashMap<String, String>()
    for (item in tableEditor.items) {
      // TODO: to prettify logical path
      if (!item.isEmpty()) {
        result[item.logicalPath] = FileUtil.toSystemIndependentName(item.directoryPath)
      }
    }
    return result
  }

  fun reset(state: CfmlProjectConfiguration.State?) {
    setItems(if (state != null) state.mapps.serverMappings else emptyMap())
    val newLanguageLevel = if (state != null) state.languageLevel else CfmlLanguage.CF10
    languageLevel.selectedItem = newLanguageLevel
  }

  fun applyTo(state: CfmlProjectConfiguration.State) {
    state.mapps = CfmlMappingsConfig(getPaths())
    state.languageLevel = languageLevel.selectedItem as String?
  }

  private class LogicalPathColumnInfo : ColumnInfo<Item, String>(CfmlBundle.message("logical.path.column.info")),
                                        ValidatingTableEditor.RowHeightProvider {
    override fun valueOf(item: Item): String {
      return item.logicalPath
    }

    override fun setValue(item: Item, value: String) {
      item.logicalPath = value
    }

    override fun isCellEditable(item: Item): Boolean {
      return true
    }

    override fun getRowHeight(): Int {
      return JTextField().preferredSize.height + 1
    }

    override fun getEditor(item: Item): TableCellEditor {
      return object : AbstractTableCellEditor() {
        private lateinit var component: JTextField

        override fun getCellEditorValue(): Any {
          return component.text
        }

        override fun getTableCellEditorComponent(table: JTable, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
          component = JTextField()
          component.text = value as String?
          return component
        }
      }
    }
  }

  private inner class DirectoryPathColumnInfo : ColumnInfo<Item, String>(CfmlBundle.message("directory.path.column.info")),
                                                ValidatingTableEditor.RowHeightProvider {
    override fun valueOf(item: Item): String {
      return item.directoryPath
    }

    override fun setValue(item: Item, value: String) {
      item.directoryPath = FileUtil.toSystemDependentName(value)
    }

    override fun isCellEditable(item: Item): Boolean {
      return true
    }

    override fun getEditor(item: Item): TableCellEditor {
      return LocalPathCellEditor(project)
    }

    override fun getRowHeight(): Int {
      return JTextField().preferredSize.height + 1
    }
  }
}
