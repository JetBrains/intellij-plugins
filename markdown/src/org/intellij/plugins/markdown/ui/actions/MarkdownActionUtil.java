package org.intellij.plugins.markdown.ui.actions;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.find.FindManager;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.ReplacePromptDialog;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElementFactory;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.intellij.plugins.markdown.lang.MarkdownElementTypes.*;

public class MarkdownActionUtil {
  @Nullable
  public static SplitFileEditor findSplitEditor(AnActionEvent e) {
    final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
    return findSplitEditor(editor);
  }

  @Nullable
  public static SplitFileEditor findSplitEditor(@Nullable FileEditor editor) {
    if (editor instanceof SplitFileEditor) {
      return (SplitFileEditor)editor;
    }
    else {
      return SplitFileEditor.PARENT_SPLIT_KEY.get(editor);
    }
  }

  @Nullable
  public static Editor findMarkdownTextEditor(AnActionEvent e) {
    final SplitFileEditor splitEditor = findSplitEditor(e);
    if (splitEditor == null) {
      // This fallback is used primarily for testing

      final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
      if (psiFile != null && psiFile.getLanguage() == MarkdownLanguage.INSTANCE && ApplicationManager.getApplication().isUnitTestMode()) {
        return e.getData(CommonDataKeys.EDITOR);
      }
      else {
        return null;
      }
    }

    if (!(splitEditor.getMainEditor() instanceof TextEditor)) {
      return null;
    }
    final TextEditor mainEditor = (TextEditor)splitEditor.getMainEditor();
    if (!mainEditor.getComponent().isVisible()) {
      return null;
    }

    return mainEditor.getEditor();
  }

  @Nullable
  public static Couple<PsiElement> getElementsUnderCaretOrSelection(@NotNull PsiFile file, @NotNull Caret caret) {
    if (caret.getSelectionStart() == caret.getSelectionEnd()) {
      final PsiElement element = file.findElementAt(caret.getSelectionStart());
      if (element == null) {
        return null;
      }
      return Couple.of(element, element);
    }
    else {
      final PsiElement startElement = file.findElementAt(caret.getSelectionStart());
      final PsiElement endElement = file.findElementAt(caret.getSelectionEnd());
      if (startElement == null || endElement == null) {
        return null;
      }
      return Couple.of(startElement, endElement);
    }
  }

  @Nullable
  public static PsiElement getCommonParentOfType(@NotNull PsiElement element1,
                                                 @NotNull PsiElement element2,
                                                 @NotNull final IElementType elementType) {
    return getCommonParentOfTypes(element1, element2, TokenSet.create(elementType));
  }

  @Nullable
  public static PsiElement getCommonTopmostParentOfTypes(@NotNull PsiElement element1,
                                                         @NotNull PsiElement element2,
                                                         @NotNull TokenSet tokenSet) {
    final PsiElement base = PsiTreeUtil.findCommonParent(element1, element2);
    return getTopmostParentOfType(base, (Condition<? super PsiElement>)element -> {
      final ASTNode node = element.getNode();
      return node != null && tokenSet.contains(node.getElementType());
    });
  }

  @Nullable
  public static PsiElement getTopmostParentOfType(@Nullable PsiElement element, @NotNull Condition<? super PsiElement> condition) {
    PsiElement answer = PsiTreeUtil.findFirstParent(element, false, condition);

    do {
      PsiElement next = PsiTreeUtil.findFirstParent(answer, true, condition);
      if (next == null) break;
      answer = next;
    }
    while (true);

    return answer;
  }

  @Nullable
  public static PsiElement getCommonParentOfTypes(@NotNull PsiElement element1,
                                                  @NotNull PsiElement element2,
                                                  @NotNull TokenSet tokenSet) {
    final PsiElement base = PsiTreeUtil.findCommonParent(element1, element2);
    return PsiTreeUtil.findFirstParent(base, false, element -> {
      final ASTNode node = element.getNode();
      return node != null && tokenSet.contains(node.getElementType());
    });
  }

  @SuppressWarnings("Duplicates")
  public static void replaceDuplicates(@NotNull PsiElement file,
                                       @NotNull Editor editor,
                                       @NotNull List<SmartPsiElementPointer<PsiElement>> duplicates,
                                       @NotNull String referenceText) {
    final String message =
      MarkdownBundle.message("markdown.extract.link.extract.duplicates.description", ApplicationNamesInfo.getInstance().getProductName(),
                             duplicates.size());
    final boolean isUnittest = ApplicationManager.getApplication().isUnitTestMode();
    final Project project = file.getProject();
    final int exitCode =
      !isUnittest ? Messages.showYesNoDialog(project, message, MarkdownBundle.message("markdown.extract.link.refactoring.dialog.title"),
                                             Messages.getInformationIcon()) : Messages.YES;

    if (exitCode == Messages.YES) {
      boolean replaceAll = false;
      final Map<PsiElement, RangeHighlighter> highlighterMap = new HashMap<>();
      for (SmartPsiElementPointer<PsiElement> smartPsiElementPointer : duplicates) {
        PsiElement match = smartPsiElementPointer.getElement();
        if (match == null) {
          continue;
        }

        if (!match.isValid()) continue;

        if (!replaceAll) {
          highlightInEditor(project, editor, highlighterMap, match);

          int promptResult = FindManager.PromptResult.ALL;
          if (!isUnittest) {
            ReplacePromptDialog promptDialog = new ReplacePromptDialog(false, MarkdownBundle.message(
              "markdown.extract.link.extract.link.replace"), project);
            promptDialog.show();
            promptResult = promptDialog.getExitCode();
          }
          if (promptResult == FindManager.PromptResult.SKIP) {
            final HighlightManager highlightManager = HighlightManager.getInstance(project);
            final RangeHighlighter highlighter = highlighterMap.get(match);
            if (highlighter != null) highlightManager.removeSegmentHighlighter(editor, highlighter);
            continue;
          }

          if (promptResult == FindManager.PromptResult.CANCEL) break;

          if (promptResult == FindManager.PromptResult.OK) {
            replaceDuplicate(match, referenceText);
          }
          else if (promptResult == FindManager.PromptResult.ALL) {
            replaceDuplicate(match, referenceText);
            replaceAll = true;
          }
        }
        else {
          replaceDuplicate(match, referenceText);
        }
      }
    }
  }

  private static void replaceDuplicate(@NotNull PsiElement match, @NotNull String referenceText) {
    WriteCommandAction.runWriteCommandAction(match.getProject(), () -> {
      PsiFile file = match.getContainingFile();
      if (!file.isValid()) {
        return;
      }

      match.replace(createLinkDeclarationAndReference(match.getProject(), match, referenceText).getFirst());
    });
  }

  @SuppressWarnings("Duplicates")
  private static void highlightInEditor(@NotNull final Project project,
                                        @NotNull final Editor editor,
                                        @NotNull Map<PsiElement, RangeHighlighter> highlighterMap,
                                        @NotNull PsiElement element) {
    final List<RangeHighlighter> highlighters = new ArrayList<>();
    final HighlightManager highlightManager = HighlightManager.getInstance(project);
    final EditorColorsManager colorsManager = EditorColorsManager.getInstance();
    final TextAttributes attributes = colorsManager.getGlobalScheme().getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);
    final int startOffset = element.getTextRange().getStartOffset();
    final int endOffset = element.getTextRange().getEndOffset();
    highlightManager.addRangeHighlight(editor, startOffset, endOffset, attributes, true, highlighters);
    highlighterMap.put(element, highlighters.get(0));
    final LogicalPosition logicalPosition = editor.offsetToLogicalPosition(startOffset);
    editor.getScrollingModel().scrollTo(logicalPosition, ScrollType.MAKE_VISIBLE);
  }

  @NotNull
  public static Pair<PsiElement, PsiElement> createLinkDeclarationAndReference(Project project, PsiElement link, String referenceText) {
    String text = null;
    String title = null;
    String url = getUrl(link);

    if (PsiUtilCore.getElementType(link) == INLINE_LINK) {
      SyntaxTraverser<PsiElement> syntaxTraverser = SyntaxTraverser.psiTraverser();

      PsiElement textElement = syntaxTraverser.children(link).find(child -> PsiUtilCore.getElementType(child) == LINK_TEXT);
      if (textElement != null) {
        text = textElement.getText();
        if (text.startsWith("[") && text.endsWith("]")) {
          text = text.substring(1, text.length() - 1);
        }
      }

      PsiElement titleElement =
        syntaxTraverser.children(link).find(child -> PsiUtilCore.getElementType(child) == LINK_TITLE);
      if (titleElement != null) {
        title = titleElement.getText();
      }
    }

    assert url != null;

    if (text == null) {
      text = url;
    }

    return MarkdownPsiElementFactory.createLinkDeclarationAndReference(project, url, text, title, referenceText);
  }

  @Nullable
  public static String getUrl(@NotNull PsiElement link) {
    String url = null;
    IElementType type = PsiUtilCore.getElementType(link);
    if (type == AUTOLINK) {
      url = link.getFirstChild().getNextSibling().getText();
    }
    else if (type == MarkdownTokenTypes.GFM_AUTOLINK) {
      url = link.getText();
    }
    else if (type == MarkdownTokenTypes.EMAIL_AUTOLINK) {
      url = link.getText();
    }
    else if (type == INLINE_LINK) {
      SyntaxTraverser<PsiElement> syntaxTraverser = SyntaxTraverser.psiTraverser();

      url = syntaxTraverser.children(link).find(child -> PsiUtilCore.getElementType(child) == LINK_DESTINATION).getText();
    }

    return url;
  }
}
