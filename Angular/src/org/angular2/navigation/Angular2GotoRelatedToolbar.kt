package org.angular2.navigation

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.IdeActions.ACTION_GOTO_RELATED
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider
import com.intellij.openapi.editor.toolbar.floating.isInsideMainEditor
import com.intellij.openapi.keymap.KeymapUtil.getShortcutText
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.presentation.java.SymbolPresentationUtil.getSymbolPresentableText
import com.intellij.psi.util.parentOfType
import com.intellij.util.ui.JBUI
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.isAngular2Context
import org.angular2.navigation.Angular2GotoRelatedProvider.Angular2GoToRelatedItem
import org.angular2.navigation.Angular2GotoRelatedProvider.Angular2GoToRelatedItemKind
import java.awt.BorderLayout
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JPanel

internal class Angular2GotoRelatedToolbarProvider : FloatingToolbarProvider {
  override val actionGroup: ActionGroup by lazy {
    Angular2GotoRelatedItemActionGroup()
  }

  override fun isApplicable(dataContext: DataContext): Boolean {
    val editor = dataContext.getData(CommonDataKeys.EDITOR)
                 ?: return false
    val file = CommonDataKeys.PSI_FILE.getData(dataContext)
               ?: return false
    return isInsideMainEditor(dataContext)
           && editor.editorKind == EditorKind.MAIN_EDITOR
           && isAngular2Context(file)
  }

  override fun register(
    dataContext: DataContext,
    component: FloatingToolbarComponent,
    parentDisposable: Disposable,
  ) {
    val editor = dataContext.getData(CommonDataKeys.EDITOR)
                 ?: return
    val caretListener = object : CaretListener {
      override fun caretPositionChanged(event: CaretEvent) {
        component.scheduleHide()
        component.scheduleShow()
      }
    }

    editor.caretModel.addCaretListener(caretListener, parentDisposable)
  }
}

private class Angular2GotoRelatedItemActionGroup : ActionGroup() {
  private var relatedItems: Map<Angular2GoToRelatedItemKind, List<Angular2GoToRelatedItem>> = mapOf()

  private val relatedItemsActions =
    Angular2GoToRelatedItemKind.entries.map(::RelatedItemsActionsSubgroup)

  private val shortcutText = ActionManager.getInstance()
    .getKeyboardShortcut(ACTION_GOTO_RELATED)
    ?.let(::getShortcutText)

  private val children = listOfNotNull(
    *relatedItemsActions.toTypedArray(),
    shortcutText?.let(::OpenPopupRelatedFilesAction)
  ).toTypedArray()

  override fun getChildren(e: AnActionEvent?): Array<out AnAction> {
    return children
  }

  override fun update(e: AnActionEvent) {
    val file = CommonDataKeys.PSI_FILE.getData(e.dataContext)
               ?: return
    val editor = CommonDataKeys.EDITOR.getData(e.dataContext)
                 ?: return
    val element = getContextElement(file, editor)
                  ?: return

    relatedItems = getRelatedItems(element)
    e.presentation.isEnabledAndVisible = relatedItems.isNotEmpty()
  }

  private fun getContextElement(
    file: PsiFile,
    editor: Editor,
  ): PsiElement? {
    val onlyTypeScriptClassInFile = Angular2SourceUtil
      .findComponentClassesInFile(file, null)
      .singleOrNull()
    if (onlyTypeScriptClassInFile != null)
      return onlyTypeScriptClassInFile

    val element = file.findElementAt(editor.caretModel.offset)
                  ?: return file
    val parentTypeScriptClass = element.parentOfType<TypeScriptClass>(true)
    if (parentTypeScriptClass != null)
      return parentTypeScriptClass

    return element
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  private val gotoRelatedProvider = Angular2GotoRelatedProvider()

  private fun getRelatedItems(element: PsiElement): Map<Angular2GoToRelatedItemKind, List<Angular2GoToRelatedItem>> {
    return gotoRelatedProvider
      .getItems(element)
      .filterIsInstance<Angular2GoToRelatedItem>()
      .groupBy { it.kind }
  }

  inner class RelatedItemsActionsSubgroup(
    private val kind: Angular2GoToRelatedItemKind,
  ) : ActionGroup() {
    var items: List<Angular2GoToRelatedItem> = emptyList()
    var children: Array<RelatedItemAction> = emptyArray()

    override fun getChildren(e: AnActionEvent?): Array<out AnAction> {
      val newItems = relatedItems[kind]

      if (e == null || newItems == null) {
        return emptyArray()
      }

      if (items != newItems) {
        items = newItems
        children = items.map(::RelatedItemAction).toTypedArray()
        adjustPresentation(e.presentation)
      }
      return children
    }

    override fun update(e: AnActionEvent) {
      adjustPresentation(e.presentation)
    }

    private fun adjustPresentation(presentation: Presentation) {
      presentation.isEnabledAndVisible = true
      presentation.isPopupGroup = items.size > 1
      val icon = items.firstNotNullOfOrNull { it.element?.getIcon(0) }
      if (icon != null)
        presentation.icon = icon

      presentation.text = Angular2Bundle.message("angular.action.goto-related.show.all", kind.displayName)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
      return ActionUpdateThread.BGT
    }
  }
}

private class RelatedItemAction(private val relatedItem: GotoRelatedItem) : AnAction(
  getElementText(relatedItem),
  null,
  relatedItem.element?.getIcon(0),
) {
  override fun actionPerformed(e: AnActionEvent) {
    relatedItem.navigate()
  }
}

@NlsSafe
private fun getElementText(item: GotoRelatedItem): String? {
  val fileName = item.element?.containingFile?.name
  if (fileName != null)
    return fileName

  val customName = item.customName
  if (customName != null)
    return customName

  val element = item.element
  if (element != null)
    return getSymbolPresentableText(element)

  return null
}

private class OpenPopupRelatedFilesAction(private val shortcutText: String) : CustomComponentAction, AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    ActionUtil.performAction(
      action = ActionManager.getInstance().getAction(ACTION_GOTO_RELATED),
      event = e
    )
  }

  override fun createCustomComponent(
    presentation: Presentation,
    place: String,
  ): JComponent {
    return OpenPopupToolbarComponent(
      action = this,
      presentation = presentation,
      place = place,
      shortcutText = shortcutText,
    )
  }

  class OpenPopupToolbarComponent(
    action: AnAction,
    presentation: Presentation,
    place: String,
    @NlsSafe shortcutText: String,
  ) : JPanel(BorderLayout(0, 0)) {
    private val button = object : ActionButtonWithText(
      action,
      presentation,
      place,
      ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE,
    ) {
      override fun getMargins(): Insets = JBUI.insets(4, 6)
    }

    init {
      add(button, BorderLayout.CENTER)
      isOpaque = false
      border = JBUI.Borders.emptyLeft(JBUI.scale(2))

      presentation.isEnabled = true
      presentation.text = shortcutText
      presentation.description = Angular2Bundle.message("angular.action.goto-related.open.popup")
    }
  }
}