package tanvd.grazi.ide.ui

import com.intellij.openapi.ui.panel.ComponentPanelBuilder
import com.intellij.ui.IdeBorderFactory
import org.jetbrains.annotations.PropertyKey
import tanvd.grazi.GraziBundle
import java.awt.*
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.Border

fun panel(layout: LayoutManager = BorderLayout(0, 0), body: JPanel.() -> Unit) = JPanel(layout).apply(body)
fun Container.panel(layout: LayoutManager = BorderLayout(0, 0), constraint: Any,
                    body: JPanel.() -> Unit): JPanel = JPanel(layout).apply(body).also { add(it, constraint) }

fun Container.panel(layout: LayoutManager = BorderLayout(0, 0), body: JPanel.() -> Unit): JPanel = JPanel(layout).apply(body).also { add(it) }

fun label(text: String, configure: JLabel.() -> Unit = {}) = JLabel(text).apply(configure)
fun Container.label(text: String, configure: JLabel.() -> Unit = {}) = JLabel(text).apply(configure).also { add(it) }

fun comment(text: String, isBelow: Boolean = true) = ComponentPanelBuilder.createCommentComponent(text, isBelow)
fun Container.comment(text: String, isBelow: Boolean = true) = ComponentPanelBuilder.createCommentComponent(text, isBelow).also { add(it) }

fun border(text: String, hasIndent: Boolean, insets: Insets): Border = IdeBorderFactory.createTitledBorder(text, hasIndent, insets).setShowLine(true)
fun padding(insets: Insets): Border = IdeBorderFactory.createEmptyBorder(insets)

fun msg(@PropertyKey(resourceBundle = GraziBundle.bundleName) key: String, vararg params: String): String = GraziBundle.message(key, *params)

//layouts
fun panelGridBag(body: JPanel.() -> Unit) = JPanel(GridBagLayout()).apply(body)

fun Container.panelGridBag(body: JPanel.() -> Unit) = JPanel(GridBagLayout()).apply(body).also { add(it) }


fun grid(rows: Int, cols: Int) = GridLayout(rows, cols)

fun gridBagConstraint(gridx: Int, gridy: Int,
                      weightx: Double, weighty: Double,
                      gridwidth: Int = GridBagConstraints.REMAINDER, gridheight: Int = 1,
                      fill: Int = GridBagConstraints.BOTH) = GridBagConstraints().apply {
    this.fill = fill
    this.gridx = gridx
    this.gridy = gridy
    this.weightx = weightx
    this.weighty = weighty
    this.gridwidth = gridwidth
    this.gridheight = gridheight
}
