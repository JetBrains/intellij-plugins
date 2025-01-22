package org.angular2.lang.expr.service.tcb

import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColorsListener
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFile
import com.intellij.ui.*
import com.intellij.util.SingleAlarm
import com.intellij.util.containers.MultiMap
import com.intellij.util.ui.UIUtil
import com.intellij.xdebugger.XDebuggerUtil
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.SourceMapping
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.AncestorEvent

internal class Angular2TranspiledTemplateInspector(
  transpiledTemplate: Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile,
  project: Project,
  disposable: Disposable
) {

  private val generatedFile = JSChangeUtil.createJSContentFromText(project, transpiledTemplate.generatedCode, JavaScriptSupportLoader.TYPESCRIPT)
    .containingFile.viewProvider.virtualFile
  private val generatedTextEditor = TextEditorProvider.getInstance().createEditor(project, generatedFile) as TextEditor
  private val generatedEditor = generatedTextEditor.editor as EditorEx

  private val sources: List<SourceInfo> = transpiledTemplate.fileMappings.values.map {
    SourceInfo(project, disposable, it.sourceFile, it.sourceMappings)
  }

  private val sourceEditorPositionListener = object : CaretListener {
    override fun caretPositionChanged(event: CaretEvent) {
      scheduleHighlight(event.newPosition, true)
    }
  }

  private class SourceInfo(
    project: Project,
    disposable: Disposable,
    val sourceFile: PsiFile,
    val sourceMappings: List<SourceMapping>
  ) {
    val sourceTextEditor = TextEditorProvider.getInstance().createEditor(project, sourceFile.viewProvider.virtualFile) as TextEditor
    val sourceEditor = sourceTextEditor.editor as EditorEx

    val sourceMappingsSorted = sourceMappings.sortedWith(
      Comparator.comparing<SourceMapping, Int> { -it.sourceOffset }.thenComparing { it -> it.sourceLength })

    val generatedMappingsSorted = sourceMappings.sortedWith(
      Comparator.comparing<SourceMapping, Int> { -it.generatedOffset }.thenComparing { it -> it.generatedLength })

    init {
      Disposer.register(disposable, sourceTextEditor)
      setupEditor(sourceEditor)
    }

  }

  private val comboBox: ComboBox<SourceInfo> = ComboBox(CollectionComboBoxModel(sources), 100)

  private val alarm: SingleAlarm

  private var positionToSelect: LogicalPosition? = null
  private var positionToSelectIsSource: Boolean = false

  private val currentHighlighters = MultiMap<MarkupModel, RangeHighlighter>()

  private var currentInfo: SourceInfo = sources.first()

  private lateinit var rightPanel: JPanel

  init {
    Disposer.register(disposable, generatedTextEditor)
    setupEditor(generatedEditor)

    alarm = SingleAlarm(Runnable {
      val p = positionToSelect
      if (p != null) {
        positionToSelect = null
        if (positionToSelectIsSource) {
          highlightGenerated(p)
        }
        else {
          highlightSource(p)
        }
      }
    }, delay = 100, parentDisposable = disposable)
    project.messageBus.connect(disposable).subscribe(EditorColorsManager.TOPIC, EditorColorsListener { updateMarkers() })

    generatedEditor.addEditorMouseMotionListener(object : EditorMouseMotionListener {
      override fun mouseMoved(event: EditorMouseEvent) {
        if (event.mouseEvent.isShiftDown) {
          scheduleHighlight(event.editor.xyToLogicalPosition(event.mouseEvent.point), false)
        }
      }
    })
    generatedEditor.caretModel.addCaretListener(object : CaretListener {
      override fun caretPositionChanged(event: CaretEvent) {
        scheduleHighlight(event.newPosition, false)
      }
    })
    updateMarkers()
  }

  private fun updateMarkers() {
    val generatedEditorMarkup = generatedEditor.markupModel
    generatedEditorMarkup.removeAllHighlighters()
    for (source in sources) {
      Angular2TranspiledTemplateVisualizer.addMarkersToGeneratedFile(source.sourceMappings, generatedEditorMarkup)

      val sourceEditorMarkup = source.sourceEditor.markupModel
      sourceEditorMarkup.removeAllHighlighters()
      Angular2TranspiledTemplateVisualizer.addMarkersToSourceFile(source.sourceMappings, sourceEditorMarkup)
    }
  }

  private fun scheduleHighlight(position: LogicalPosition, positionToSelectIsSource: Boolean) {
    positionToSelect = position
    this.positionToSelectIsSource = positionToSelectIsSource
    alarm.cancelAndRequest()
  }


  fun createMainComponent(): JComponent {

    comboBox.renderer = SimpleListCellRenderer.create { label, value, _ ->
      if (value != null) {
        label.text = value.sourceFile.name
      }
    }
    comboBox.addItemListener {
      showSource(it.item as SourceInfo)
    }

    rightPanel = JPanel(BorderLayout())
    rightPanel.add(comboBox, BorderLayout.PAGE_START)

    val splitter = JBSplitter()
    splitter.splitterProportionKey = "SourceMapInspector.splitter"
    splitter.firstComponent = rightPanel
    splitter.secondComponent = generatedEditor.component

    generatedEditor.component.addAncestorListener(object : AncestorListenerAdapter() {
      override fun ancestorAdded(event: AncestorEvent) {
        generatedEditor.component.removeAncestorListener(this)
        if (positionToSelect == null) {
          return
        }

        highlightGenerated(positionToSelect!!)
        positionToSelect = null
        positionToSelectIsSource = false
      }
    })
    showSource(currentInfo)
    return splitter
  }

  private fun removeHighlighters() {
    if (currentHighlighters.isEmpty) {
      return
    }

    for (markupModel in currentHighlighters.keySet()) {
      for (rangeHighlighter in currentHighlighters.get(markupModel)) {
        if (rangeHighlighter.isValid) {
          markupModel.removeHighlighter(rangeHighlighter)
        }
      }
    }

    currentHighlighters.clear()
  }

  private fun highlightGenerated(sourcePosition: LogicalPosition) {
    removeHighlighters()

    val sourceOffset = currentInfo.sourceEditor.logicalPositionToOffset(sourcePosition)

    val mapping = currentInfo.sourceMappingsSorted.firstOrNull { it.sourceOffset <= sourceOffset && sourceOffset < it.sourceOffset + it.sourceLength }
                  ?: return

    addSelectedMapping(mapping)

    generatedEditor.scrollingModel.scrollTo(generatedEditor.offsetToLogicalPosition(mapping.generatedOffset), ScrollType.CENTER)
  }

  private fun highlightSource(generatedPosition: LogicalPosition) {
    removeHighlighters()

    val generatedOffset = generatedEditor.logicalPositionToOffset(generatedPosition)

    for (source in sources) {
      val mapping = source.generatedMappingsSorted.firstOrNull { it.generatedOffset <= generatedOffset && generatedOffset < it.generatedOffset + it.generatedLength }
                    ?: continue

      showSource(source)
      addSelectedMapping(mapping)
      source.sourceEditor.scrollingModel.scrollTo(currentInfo.sourceEditor.offsetToLogicalPosition(mapping.sourceOffset), ScrollType.CENTER)
    }
  }

  private fun addSelectedMapping(mapping: SourceMapping) {
    addSelectedHighlighter(currentInfo.sourceEditor.markupModel, mapping.sourceOffset, mapping.sourceOffset + mapping.sourceLength)
    currentInfo.sourceMappings.filter {
      it.sourceOffset == mapping.sourceOffset && it.sourceLength == mapping.sourceLength
    }.forEach {
      addSelectedHighlighter(generatedEditor.markupModel, it.generatedOffset, it.generatedOffset + it.generatedLength)
    }
  }

  private fun addSelectedHighlighter(markupModel: MarkupModel, startOffset: Int, endOffset: Int) {
    if (startOffset == endOffset) {
      return
    }
    else if (endOffset < startOffset) {
      throw IllegalStateException()
    }

    val highlighter = markupModel.addRangeHighlighter(startOffset, endOffset, HighlighterLayer.LAST + 100, Angular2TranspiledTemplateVisualizer.SELECTED_MARKER, HighlighterTargetArea.EXACT_RANGE)
    currentHighlighters.putValue(markupModel, highlighter)
  }

  private fun showSource(source: SourceInfo) {
    currentInfo.sourceEditor.caretModel.removeCaretListener(sourceEditorPositionListener)
    rightPanel.remove(currentInfo.sourceEditor.component)

    comboBox.selectedItem = source
    currentInfo = source
    currentInfo.sourceEditor.caretModel.addCaretListener(sourceEditorPositionListener)
    rightPanel.add(currentInfo.sourceEditor.component)
    rightPanel.repaint()
  }


  internal object Angular2TranspiledTemplateVisualizer {
    private val UNMAPPED_MARKER = createTextAttributes(JBColor.LIGHT_GRAY)
    val SELECTED_MARKER: TextAttributes = TextAttributes(JBColor.WHITE, JBColor.BLACK, JBColor.BLACK, EffectType.ROUNDED_BOX, Font.PLAIN)
    private val BASE_COLORS = arrayOf(JBColor.RED, JBColor.YELLOW, JBColor.GREEN, JBColor.CYAN, JBColor.BLUE, JBColor.MAGENTA)

    fun addMarkersToGeneratedFile(sourceMappings: List<SourceMapping>, markupModel: MarkupModel) {
      addHighlighters(sourceMappings, true, markupModel)
    }

    fun addMarkersToSourceFile(sourceMappings: List<SourceMapping>, markupModel: MarkupModel) {
      addHighlighters(sourceMappings, false, markupModel)
    }

    private fun createTextAttributes(color: Color): TextAttributes {
      return TextAttributes(null, color, JBColor.WHITE, EffectType.BOXED, Font.PLAIN)
    }

    private fun getTextAttributes(palette: Array<TextAttributes?>, line: Int): TextAttributes? {
      return if (line == -1) {
        UNMAPPED_MARKER
      }
      else {
        palette[line % palette.size]
      }
    }

    private fun createTextAttributesForEditor(transparency: Double): Array<TextAttributes?> {
      val background = EditorColorsManager.getInstance().globalScheme.defaultBackground
      val resultAttributes = arrayOfNulls<TextAttributes>(BASE_COLORS.size)
      for (i in resultAttributes.indices) {
        resultAttributes[i] = createTextAttributes(UIUtil.makeTransparent(
          BASE_COLORS[i], background, transparency))
      }
      return resultAttributes
    }

    private fun addHighlighters(mappings: List<SourceMapping>,
                                isGeneratedEditor: Boolean,
                                markupModel: MarkupModel) {
      val textStyles = createTextAttributesForEditor(0.4)
      val ignoredTextStyles = createTextAttributesForEditor(0.15)
      val colors = mutableMapOf<Pair<Int, Int>, Int>()

      fun getColorId(offset: Int, length: Int) =
        colors.computeIfAbsent(Pair(offset, length)) { colors.size }

      for (i in mappings.indices) {
        val mapping = mappings[i]

        try {
          markupModel.addRangeHighlighter(if (isGeneratedEditor) mapping.generatedOffset else mapping.sourceOffset,
                                          if (isGeneratedEditor) mapping.generatedOffset + mapping.generatedLength
                                          else mapping.sourceOffset + mapping.sourceLength,
                                          HighlighterLayer.LAST,
                                          getTextAttributes(if (isGeneratedEditor && mapping.ignored)
                                                              ignoredTextStyles
                                                            else
                                                              textStyles,
                                                            getColorId(mapping.sourceOffset, mapping.sourceLength)),
                                          HighlighterTargetArea.EXACT_RANGE)
        }
        catch (e: Exception) {
          this.thisLogger().error(e)
        }
      }
    }
  }

}

private fun setupEditor(editor: EditorEx) {
  editor.isRendererMode = true
  editor.settings.isLineNumbersShown = true
  XDebuggerUtil.getInstance().disableValueLookup(editor)
}