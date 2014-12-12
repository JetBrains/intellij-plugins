package com.jetbrains.lang.dart.validation.fixes;

import com.google.dart.server.generated.types.*;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementRenderer;
import com.intellij.codeInsight.template.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public final class DartServerFixIntention implements IntentionAction {

  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.validation.fixes.DartServerFixIntention");

  private static class SuggestionInfo {
    @NotNull
    final Position position;
    @NotNull
    final List<LinkedEditSuggestion> suggestions;

    private String[] myValues;

    SuggestionInfo(@NotNull final Position position, @NotNull final List<LinkedEditSuggestion> suggestions) {
      this.position = position;
      this.suggestions = suggestions;
    }

    @Override
    public String toString() {
      return position + " : " + suggestions;
    }

    int getOffset() {
      return position.getOffset();
    }

    String getDefaultValue() {
      return suggestions.get(0).getValue();
    }

    SuggestionKind getKind() {
      return SuggestionKind.valueOf(suggestions.get(0).getKind());
    }

    String[] getValues() {
      if (myValues == null) {
        myValues = new String[suggestions.size()];
        for (int i = 0; i < myValues.length; ++i) {
          myValues[i] = suggestions.get(i).getValue();
        }
      }
      return myValues;
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  enum SuggestionKind {

    METHOD(PlatformIcons.METHOD_ICON), PARAMETER(PlatformIcons.PARAMETER_ICON), TYPE(PlatformIcons.CLASS_ICON), VARIABLE(
      PlatformIcons.VARIABLE_ICON);

    private final Icon myIcon;

    SuggestionKind(Icon icon) {
      myIcon = icon;
    }

    Icon getIcon() {
      return myIcon;
    }
  }


  private static class DartLookupExpression extends Expression {

    @NotNull
    private final SuggestionInfo mySuggestion;

    DartLookupExpression(@NotNull SuggestionInfo suggestion) {
      mySuggestion = suggestion;
    }

    @Override
    public Result calculateQuickResult(ExpressionContext context) {
      final String selection = context.getProperty(ExpressionContext.SELECTION);
      return new TextResult(selection == null ? mySuggestion.getDefaultValue() : selection);
    }

    @Override
    public Result calculateResult(ExpressionContext context) {
      return calculateQuickResult(context);
    }

    @Nullable
    @Override
    public LookupElement[] calculateLookupItems(final ExpressionContext context) {

      final String[] values = mySuggestion.getValues();
      final LookupElement[] elements = new LookupElement[values.length];
      for (int i = 0; i < values.length; i++) {
        elements[i] = LookupElementBuilder.create(values[i]).withRenderer(new LookupElementRenderer<LookupElement>() {
          @Override
          public void renderElement(final LookupElement element, final LookupElementPresentation presentation) {
            presentation.setIcon(mySuggestion.getKind().getIcon());
            presentation.setItemText(element.getLookupString());
          }
        });
      }
      return elements;
    }
  }

  @NotNull
  private final SourceChange myChange;

  public DartServerFixIntention(@NotNull final SourceChange change) {
    myChange = change;
  }

  @NotNull
  @Override
  public String getText() {
    return myChange.getMessage();
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return getText();
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return true;
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }

  @Override
  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    final Document document = editor.getDocument();
    for (SourceFileEdit fileEdit : myChange.getEdits()) {
      for (SourceEdit edit : fileEdit.getEdits()) {

        // Templates can only grow source, so we trim first if necessary
        if (edit.getLength() > 0) {
          document.deleteString(edit.getOffset(), edit.getOffset() + edit.getLength());
        }

        final TemplateManager templateManager = TemplateManager.getInstance(project);
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        addContents(template, edit);

        try {
          final VirtualFile virtualFile = VfsUtil.findFileByURL(new URL("file://" + fileEdit.getFile()));
          if (virtualFile != null) {
            final Editor targetEditor = BaseCreateFix.navigate(project, edit.getOffset(), virtualFile);
            if (targetEditor != null) {
              templateManager.startTemplate(targetEditor, template);
            }
          }
        }
        catch (MalformedURLException e) {
          LOG.error(e);
        }
      }
    }
  }

  private void addContents(final Template template, final SourceEdit edit) {

    final String replacementText = edit.getReplacement();
    final int initialOffset = edit.getOffset();

    final List<SuggestionInfo> suggestions = extractSuggestions(myChange.getLinkedEditGroups());

    int currentInsertOffset = 0;

    for (SuggestionInfo suggestion : suggestions) {
      String text = replacementText.substring(currentInsertOffset, suggestion.getOffset() - initialOffset);
      template.addTextSegment(text);
      template.addVariable(new DartLookupExpression(suggestion), true);
      currentInsertOffset += text.length() + suggestion.getDefaultValue().length();
    }
    if (currentInsertOffset < replacementText.length()) {
      final String text = replacementText.substring(currentInsertOffset);
      template.addTextSegment(text);
    }

    //TODO: use myChange.getSelection() to set an end variable

  }

  private static List<SuggestionInfo> extractSuggestions(final List<LinkedEditGroup> editGroups) {
    List<SuggestionInfo> info = new ArrayList<SuggestionInfo>();
    for (LinkedEditGroup editGroup : editGroups) {
      final List<LinkedEditSuggestion> suggestions = editGroup.getSuggestions();
      if (!suggestions.isEmpty()) {
        final List<Position> positions = editGroup.getPositions();
        if (!positions.isEmpty()) {
          info.add(new SuggestionInfo(positions.get(0), suggestions));
        }
      }
    }
    return info;
  }

}
