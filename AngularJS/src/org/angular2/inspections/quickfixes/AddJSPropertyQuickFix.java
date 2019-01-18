// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2InjectionUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.JSTokenTypes.COMMA;

public class AddJSPropertyQuickFix extends LocalQuickFixOnPsiElement {
  private static final String CARET_MARKER = "___caret___";

  private final String myPropertyName;
  private final boolean myUseTemplateString;
  private final String myValue;
  private final int myCaretOffset;

  public AddJSPropertyQuickFix(@NotNull JSObjectLiteralExpression objectLiteral,
                               @NotNull String name,
                               @NotNull String value,
                               int caretOffset,
                               boolean useTemplateString) {
    super(objectLiteral);
    myPropertyName = name;
    myUseTemplateString = useTemplateString;
    myValue = value;
    myCaretOffset = caretOffset;
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  @NotNull
  @Override
  public String getText() {
    return "Add '" + myPropertyName + "' property";
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  @NotNull
  @Override
  public String getFamilyName() {
    return "Angular";
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull PsiFile file, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
    JSObjectLiteralExpression objectLiteral = ObjectUtils.tryCast(startElement, JSObjectLiteralExpression.class);
    if (objectLiteral != null) {
      String quote;
      if (myUseTemplateString) {
        quote = "`";
      }
      else {
        quote = JSCodeStyleSettings.getQuote(objectLiteral);
      }
      String value = quote + myValue.substring(0, myCaretOffset) +
                     CARET_MARKER + myValue.substring(myCaretOffset) + quote;
      PsiElement property = JSChangeUtil.createObjectLiteralPropertyFromText(myPropertyName + ": " + value, objectLiteral);
      JSProperty added = (JSProperty)JSRefactoringUtil.addMemberToMemberHolder(objectLiteral, property, objectLiteral);
      formatNewLinesAroundProperty(added);


      SmartPsiElementPointer<JSProperty> propertyPointer =
        SmartPointerManager.getInstance(project).createSmartPsiElementPointer(added);

      FormatFixer.create(objectLiteral, FormatFixer.Mode.Reformat).fixFormat();

      added = propertyPointer.getElement();
      assert added != null;
      Document document = PsiDocumentManager.getInstance(project).getDocument(added.getContainingFile());
      assert document != null;
      PsiDocumentManager.getInstance(project).commitDocument(document);
      PsiFile htmlContent = Angular2InjectionUtils.getFirstInjectedFile(added.getValue());
      if (htmlContent != null) {
        FormatFixer.create(htmlContent, FormatFixer.Mode.Reformat).fixFormat();
      }

      added = propertyPointer.getElement();
      assert added != null;
      JSExpression valueExpression = added.getValue();
      assert valueExpression != null;

      int caretOffset = valueExpression.getTextOffset() + valueExpression.getText().indexOf(CARET_MARKER);
      document.replaceString(caretOffset, caretOffset + CARET_MARKER.length(), "");
      PsiDocumentManager.getInstance(project).commitDocument(document);

      PsiNavigationSupport.getInstance().createNavigatable(
        project, added.getContainingFile().getVirtualFile(), caretOffset).
        navigate(true);
    }
  }

  private static void formatNewLinesAroundProperty(@NotNull JSProperty property) {
    JSObjectLiteralExpression initializer = (JSObjectLiteralExpression)property.getParent();
    boolean wrapWithNewLines = ContainerUtil.find(
      initializer.getProperties(), p -> p != property && !isPrefixedWithNewLine(p)) == null
                               || (property.getValue() != null && property.getValue().getText().contains("\n"));

    if (wrapWithNewLines) {
      if (!isPrefixedWithNewLine(property)) {
        JSChangeUtil.addWs(property.getParent().getNode(), property.getNode(), "\n");
      }
      LeafPsiElement comma = findComma(property);
      if (comma != null) {
        PsiElement next = comma.getNextSibling();
        if (!(next instanceof PsiWhiteSpace) || !next.getText().contains("\n")) {
          JSChangeUtil.addWsAfter(property.getParent(), comma, "\n");
        }
      }
    }
  }

  private static boolean isPrefixedWithNewLine(@NotNull JSProperty property) {
    PsiWhiteSpace whiteSpace = ObjectUtils.tryCast(property.getPrevSibling(), PsiWhiteSpace.class);
    return whiteSpace != null && whiteSpace.getText().contains("\n");
  }

  @Nullable
  private static LeafPsiElement findComma(@NotNull JSProperty property) {
    PsiElement el = property.getNextSibling();
    while (el instanceof PsiWhiteSpace) {
      el = el.getNextSibling();
    }
    if (!(el instanceof LeafPsiElement)
        || el.getNode().getElementType() != COMMA) {
      return null;
    }
    return (LeafPsiElement)el;
  }
}
