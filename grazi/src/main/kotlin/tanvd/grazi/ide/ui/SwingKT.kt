package tanvd.grazi.ide.ui

import com.intellij.ui.components.JBTabbedPane
import java.awt.*
import javax.swing.*
import javax.swing.border.Border
import javax.swing.border.CompoundBorder

fun panel(layout: LayoutManager = BorderLayout(0, 0), body: JPanel.() -> Unit) = JPanel(layout).apply(body)
fun JTabbedPane.panel(layout: LayoutManager = BorderLayout(0, 0), body: JPanel.() -> Unit) = JPanel(layout).apply(body)
fun Container.panel(layout: LayoutManager = BorderLayout(0, 0), constraint: Any,
                    body: JPanel.() -> Unit): JPanel = JPanel(layout).apply(body).also { add(it, constraint) }

fun tabs(body: JBTabbedPane.() -> Unit) = JBTabbedPane().apply(body)
fun Container.tabs(body: JBTabbedPane.() -> Unit) = JBTabbedPane().apply(body).also { add(it) }

fun JTabbedPane.tab(name: String, body: JTabbedPane.() -> Component) = body().also { addTab(name, it) }

fun label(text: String, configure: JLabel.() -> Unit = {}) = JLabel(text).apply(configure)
fun Container.label(text: String, configure: JLabel.() -> Unit = {}) = JLabel(text).apply(configure).also { add(it) }

private val panelEmptyBorder: Border = BorderFactory.createEmptyBorder(3, 5, 5, 5)
fun border(text: String): CompoundBorder = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(text), panelEmptyBorder)
