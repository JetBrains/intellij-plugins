package org.angular2.lang.html.tcb

import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.openapi.Disposable
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
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.AncestorListenerAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.util.SingleAlarm
import com.intellij.util.containers.MultiMap
import com.intellij.util.ui.UIUtil
import com.intellij.xdebugger.XDebuggerUtil
import org.angular2.lang.html.Angular17HtmlLanguage
import org.angular2.lang.html.tcb.Angular2TemplateTranspiler.SourceMapping
import org.angular2.lang.html.tcb.Angular2TemplateTranspiler.TranspiledTemplate
import java.awt.Color
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.event.AncestorEvent

internal class Angular2TranspiledTemplateInspector(
  private val transpiledTemplate: TranspiledTemplate,
  project: Project,
  disposable: Disposable
) {

  private val generatedFile = JSChangeUtil.createJSContentFromText(project, transpiledTemplate.generatedCode, JavaScriptSupportLoader.TYPESCRIPT)
    .containingFile.viewProvider.virtualFile
  private val generatedTextEditor = TextEditorProvider.getInstance().createEditor(project, generatedFile) as TextEditor
  private val generatedEditor = generatedTextEditor.editor as EditorEx


  private val sourceFile = PsiFileFactory.getInstance(project).createFileFromText(
    JSUtils.DUMMY_FILE_NAME_PREFIX + ".html", Angular17HtmlLanguage,
    transpiledTemplate.sourceCode, false, true)
    .viewProvider.virtualFile
  private val sourceTextEditor = TextEditorProvider.getInstance().createEditor(project, sourceFile) as TextEditor
  private val sourceEditor = sourceTextEditor.editor as EditorEx

  private val alarm: SingleAlarm

  private var positionToSelect: LogicalPosition? = null
  private var positionToSelectIsSource: Boolean = false

  private val currentHighlighters = MultiMap<MarkupModel, RangeHighlighter>()

  private val sourceMappingsSorted = transpiledTemplate.sourceMappings.sortedBy { -it.sourceOffset }
  private val generatedMappingsSorted = transpiledTemplate.sourceMappings.sortedBy { -it.generatedOffset }

  init {
    Disposer.register(disposable, generatedTextEditor)
    Disposer.register(disposable, sourceTextEditor)

    setupEditor(generatedEditor)
    setupEditor(sourceEditor)
    generatedEditor.headerComponent = JLabel("0:0", SwingConstants.RIGHT)

    Angular2TranspiledTemplateVisualizer.addMarkersToGeneratedFile(transpiledTemplate, generatedEditor.markupModel)
    Angular2TranspiledTemplateVisualizer.addMarkersToSourceFile(transpiledTemplate, sourceEditor.markupModel)

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

    sourceEditor.caretModel.addCaretListener(object : CaretListener {
      override fun caretPositionChanged(event: CaretEvent) {
        scheduleHighlight(event.newPosition, true)
      }
    })
  }

  private fun updateMarkers() {
    val generatedEditorMarkup = generatedEditor.markupModel
    generatedEditorMarkup.removeAllHighlighters()
    Angular2TranspiledTemplateVisualizer.addMarkersToGeneratedFile(transpiledTemplate, generatedEditorMarkup)

    val sourceEditorMarkup = sourceEditor.markupModel
    sourceEditorMarkup.removeAllHighlighters()
    Angular2TranspiledTemplateVisualizer.addMarkersToSourceFile(transpiledTemplate, sourceEditorMarkup)
  }

  private fun scheduleHighlight(position: LogicalPosition, positionToSelectIsSource: Boolean) {
    positionToSelect = position
    this.positionToSelectIsSource = positionToSelectIsSource
    alarm.cancelAndRequest()
  }

  fun createMainComponent(): JComponent {
    val splitter = JBSplitter()
    splitter.splitterProportionKey = "SourceMapInspector.splitter"
    splitter.firstComponent = sourceEditor.component
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

    val sourceOffset = sourceEditor.logicalPositionToOffset(sourcePosition)

    val mapping = sourceMappingsSorted.firstOrNull { it.sourceOffset <= sourceOffset && sourceOffset < it.sourceOffset + it.sourceLength }
                  ?: return

    addSelectedMapping(mapping)

    generatedEditor.scrollingModel.scrollTo(generatedEditor.offsetToLogicalPosition(mapping.sourceOffset), ScrollType.CENTER)
  }

  private fun highlightSource(generatedPosition: LogicalPosition) {
    removeHighlighters()

    val generatedOffset = generatedEditor.logicalPositionToOffset(generatedPosition)

    val mapping = generatedMappingsSorted.firstOrNull { it.generatedOffset <= generatedOffset && generatedOffset < it.generatedOffset + it.generatedLength }
                  ?: return

    addSelectedMapping(mapping)
    sourceEditor.scrollingModel.scrollTo(sourceEditor.offsetToLogicalPosition(mapping.sourceOffset), ScrollType.CENTER)
  }

  private fun addSelectedMapping(mapping: SourceMapping) {
    addSelectedHighlighter(sourceEditor.markupModel, mapping.sourceOffset, mapping.sourceOffset + mapping.sourceLength )
    transpiledTemplate.sourceMappings.filter {
      it.sourceOffset == mapping.sourceOffset && it.sourceLength == mapping.sourceLength
    }.forEach {
      addSelectedHighlighter(generatedEditor.markupModel, it.generatedOffset, it.generatedOffset + it.generatedLength )
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

  private fun setupEditor(editor: EditorEx) {
    editor.isRendererMode = true
    editor.settings.isLineNumbersShown = true
    XDebuggerUtil.getInstance().disableValueLookup(editor)
  }


  internal object Angular2TranspiledTemplateVisualizer {
    private val UNMAPPED_MARKER = createTextAttributes(JBColor.LIGHT_GRAY)
    val SELECTED_MARKER: TextAttributes = TextAttributes(JBColor.WHITE, JBColor.BLACK, JBColor.BLACK, EffectType.ROUNDED_BOX, Font.PLAIN)
    private val BASE_COLORS = arrayOf(JBColor.RED, JBColor.YELLOW, JBColor.GREEN, JBColor.CYAN, JBColor.BLUE, JBColor.MAGENTA)

    fun addMarkersToGeneratedFile(template: TranspiledTemplate, markupModel: MarkupModel) {
      addHighlighters(template.sourceMappings, true, markupModel)
    }

    fun addMarkersToSourceFile(template: TranspiledTemplate, markupModel: MarkupModel) {
      addHighlighters(template.sourceMappings, false, markupModel)
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

    private fun createTextAttributesForEditor(): Array<TextAttributes?> {
      val background = EditorColorsManager.getInstance().globalScheme.defaultBackground
      val resultAttributes = arrayOfNulls<TextAttributes>(BASE_COLORS.size)
      for (i in resultAttributes.indices) {
        resultAttributes[i] = createTextAttributes(UIUtil.makeTransparent(
          BASE_COLORS[i], background, 0.4))
      }
      return resultAttributes
    }

    private fun addHighlighters(mappings: List<SourceMapping>,
                                isGeneratedEditor: Boolean,
                                markupModel: MarkupModel) {
      val textStyles = createTextAttributesForEditor()
      val colors = mutableMapOf<Pair<Int, Int>, Int>()

      fun getColorId(offset: Int, length:Int) =
        colors.computeIfAbsent(Pair(offset, length)) {colors.size}

      for (i in mappings.indices) {
        val mapping = mappings[i]

        markupModel.addRangeHighlighter(if (isGeneratedEditor) mapping.generatedOffset else mapping.sourceOffset,
                                        if (isGeneratedEditor) mapping.generatedOffset + mapping.generatedLength
                                        else mapping.sourceOffset + mapping.sourceLength,
                                        HighlighterLayer.LAST,
                                        getTextAttributes(textStyles, getColorId(mapping.sourceOffset, mapping.sourceLength)),
                                        HighlighterTargetArea.EXACT_RANGE)
      }
    }
  }

}