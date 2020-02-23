// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui

import com.intellij.openapi.application.runInEdt
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.paint.RectanglePainter
import com.intellij.util.ui.TimerUtil
import java.awt.*
import java.util.*
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.SwingUtilities
import kotlin.math.absoluteValue

object LearningUiHighlightingManager {
  data class HighlightingOptions(val highlightBorder: Boolean = true, val highlightInside: Boolean = true)

  private val highlights: MutableList<RepaintByTimer> = ArrayList()

  fun highlightComponent(original: Component, options: HighlightingOptions = HighlightingOptions()) {
    runInEdt {
      clearHighlights()
      val glassPane = getGlassPane(original) ?: return@runInEdt
      val repaintByTimer = RepaintByTimer(original, glassPane, options)
      repaintByTimer.reinitHighlightComponent()
      highlights.add(repaintByTimer)
    }
  }

  fun highlightJListItem(list: JList<*>, index: () -> Int, options: HighlightingOptions = HighlightingOptions()) {
    runInEdt {
      clearHighlights()
      val glassPane = getGlassPane(list) ?: return@runInEdt
      val repaintByTimer = RepaintCellByTimer(list, index, glassPane, options)
      repaintByTimer.reinitHighlightComponent()
      highlights.add(repaintByTimer)
    }
  }

  fun clearHighlights() {
    runInEdt {
      for (core in highlights) {
        removeIt(core)
      }
      highlights.clear()
    }
  }

  internal fun removeIt(core: RepaintByTimer) {
    core.removed = true
    core.cleanup()
  }
}

internal open class RepaintByTimer(val original: Component,
                                   val glassPane: JComponent,
                                   val options: LearningUiHighlightingManager.HighlightingOptions) {
  var removed = false
  protected val startDate = Date()

  protected lateinit var highlightComponent: GlassHighlightComponent

  init {
    initTimer()
  }

  open fun reinitHighlightComponent() {
    highlightComponent = GlassHighlightComponent(startDate, options)

    val pt = SwingUtilities.convertPoint(original, Point(0, 0), glassPane)
    val bounds = Rectangle(pt.x, pt.y, original.width, original.height)

    highlightComponent.bounds = bounds
    glassPane.add(highlightComponent)
  }

  private fun initTimer() {
    val timer = TimerUtil.createNamedTimer("IFT item", 50)
    timer.addActionListener {
      if (!isShowing()) {
        LearningUiHighlightingManager.removeIt(this)
      }
      if (this.removed) {
        timer.stop()
        return@addActionListener
      }
      if (highlightComponent.isValid && original.locationOnScreen != highlightComponent.locationOnScreen || original.size != highlightComponent.size) {
        cleanup()
        reinitHighlightComponent()
      }
      highlightComponent.repaint()
    }
    timer.start()
  }

  protected open fun isShowing(): Boolean {
    return original.isShowing
  }

  fun cleanup() {
    if (glassPane.isValid) {
      glassPane.remove(highlightComponent)
      glassPane.revalidate()
      glassPane.repaint()
    }
  }
}

internal class RepaintCellByTimer(val list: JList<*>,
                                  private val index: () -> Int,
                                  glassPane: JComponent,
                                  options: LearningUiHighlightingManager.HighlightingOptions)
  : RepaintByTimer(list, glassPane, options) {

  override fun isShowing(): Boolean {
    return super.isShowing() && list.visibleRowCount > index()
  }

  override fun reinitHighlightComponent() {
    val i = index()
    if (i == -1) {
      return
    }
    highlightComponent = GlassHighlightComponent(startDate, options)

    val cellBounds = list.getCellBounds(i, i)

    val pt = SwingUtilities.convertPoint(original, cellBounds.location, glassPane)
    val bounds = Rectangle(pt.x, pt.y, cellBounds.width, cellBounds.height)

    highlightComponent.bounds = bounds
    glassPane.add(highlightComponent)
  }
}

internal class GlassHighlightComponent(private val startDate: Date,
                                       private val options: LearningUiHighlightingManager.HighlightingOptions) : JComponent() {
  override fun paintComponent(g: Graphics) {
    val g2d = g as Graphics2D
    val r: Rectangle = bounds
    val oldColor = g2d.color
    val delta = Date().time - startDate.time
    fun cyclicNumber(amplitude: Int, change: Long) = (change % (2 * amplitude) - amplitude).absoluteValue.toInt()
    val gradientShift = (delta / 20).toFloat()
    val alphaCycle = cyclicNumber(1000, delta).toDouble() / 1000
    val magenta = ColorUtil.withAlpha(Color.magenta, 0.8)
    val orange = ColorUtil.withAlpha(Color.orange, 0.8)
    val background = ColorUtil.withAlpha(JBColor(Color.black, Color.white), 0.3 * alphaCycle)
    val gp = GradientPaint(gradientShift + 0F, gradientShift + 0F, magenta,
                           gradientShift + r.height.toFloat(), gradientShift + r.height.toFloat(), orange, true)
    RectanglePainter.paint(g2d, 0, 0, r.width, r.height, 2,
                           if (options.highlightInside) background else null,
                           if (options.highlightBorder) gp else null)
    g2d.color = oldColor
  }
}

private fun getGlassPane(component: Component): JComponent? {
  val rootPane = SwingUtilities.getRootPane(component)
  return if (rootPane == null) null else rootPane.glassPane as JComponent
}
