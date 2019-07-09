package tanvd.grazi.ide.ui

import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBTabbedPane
import org.jetbrains.annotations.PropertyKey
import tanvd.grazi.GraziBundle
import java.awt.*
import javax.swing.*
import javax.swing.border.Border

fun panel(layout: LayoutManager = BorderLayout(0, 0), body: JPanel.() -> Unit) = JPanel(layout).apply(body)
fun JTabbedPane.panel(layout: LayoutManager = BorderLayout(0, 0), body: JPanel.() -> Unit) = JPanel(layout).apply(body)
fun Container.panel(layout: LayoutManager = BorderLayout(0, 0), constraint: Any,
                    body: JPanel.() -> Unit): JPanel = JPanel(layout).apply(body).also { add(it, constraint) }

fun tabs(body: JBTabbedPane.() -> Unit) = JBTabbedPane().apply(body)
fun Container.tabs(body: JBTabbedPane.() -> Unit) = JBTabbedPane().apply(body).also { add(it) }

fun JTabbedPane.tab(name: String, body: JTabbedPane.() -> Component) = body().also { addTab(name, it) }

fun label(text: String, configure: JLabel.() -> Unit = {}) = JLabel(text).apply(configure)
fun Container.label(text: String, configure: JLabel.() -> Unit = {}) = JLabel(text).apply(configure).also { add(it) }

fun border(text: String, hasIndent: Boolean = false): Border = IdeBorderFactory.createTitledBorder(text, hasIndent).setShowLine(true)
fun border(text: String, hasIndent: Boolean, insets: Insets): Border = IdeBorderFactory.createTitledBorder(text, hasIndent, insets).setShowLine(true)

fun msg(@PropertyKey(resourceBundle = GraziBundle.bundleName) key: String, vararg params: String): String = GraziBundle.message(key, *params)
