package tanvd.grazi.ide.ui.components.dsl

import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.panel.ComponentPanelBuilder
import com.intellij.ui.IdeBorderFactory
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.PropertyKey
import tanvd.grazi.GraziBundle
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Insets
import java.awt.LayoutManager
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.border.Border

fun panel(layout: LayoutManager = BorderLayout(0, 0), body: JPanel.() -> Unit) = JPanel(layout).apply(body)
fun Container.panel(layout: LayoutManager = BorderLayout(0, 0), constraint: Any,
                    body: JPanel.() -> Unit): JPanel = JPanel(layout).apply(body).also { add(it, constraint) }

fun border(text: String, hasIndent: Boolean, insets: Insets, showLine: Boolean = true): Border = IdeBorderFactory.createTitledBorder(text, hasIndent, insets).setShowLine(showLine)

fun padding(insets: Insets): Border = IdeBorderFactory.createEmptyBorder(insets)

fun wrapWithComment(component: JComponent, comment: String) = ComponentPanelBuilder(component).withComment(comment).resizeY(true).createPanel()
fun wrapWithLabel(component: JComponent, label: String) = ComponentPanelBuilder(component).withLabel(label).resizeY(true).createPanel()
fun wrap(component: JComponent, comment: String, label: String) = ComponentPanelBuilder(component).withComment(comment).withLabel(label).resizeY(true).createPanel()

fun msg(@PropertyKey(resourceBundle = GraziBundle.bundleName) key: String, vararg params: String): String = GraziBundle.message(key, *params)

fun pane() = JEditorPane().apply {
    editorKit = UIUtil.getHTMLEditorKit()
    isEditable = false
    isOpaque = true
    border = null
    background = null
}

fun actionGroup(body: DefaultActionGroup.() -> Unit) = DefaultActionGroup().apply(body)
