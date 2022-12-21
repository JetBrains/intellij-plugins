// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static org.angular2.inspections.quickfixes.Angular2FixesPsiUtil.insertJSObjectLiteralProperty;
import static org.angular2.inspections.quickfixes.Angular2FixesPsiUtil.reformatJSObjectLiteralProperty;

public class AddJSPropertyQuickFix extends LocalQuickFixOnPsiElement {
  @NonNls private static final String CARET_MARKER = "___caret___";

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

  @Override
  public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getText() {
    return Angular2Bundle.message("angular.quickfix.decorator.add-property.name", myPropertyName);
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
    return Angular2Bundle.message("angular.quickfix.decorator.add-property.family");
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

      JSProperty added = reformatJSObjectLiteralProperty(insertJSObjectLiteralProperty(objectLiteral, myPropertyName, value));
      JSExpression valueExpression = added.getValue();
      assert valueExpression != null;

      PsiDocumentManager documentManager = PsiDocumentManager.getInstance(valueExpression.getProject());
      int caretOffset = valueExpression.getTextOffset() + valueExpression.getText().indexOf(CARET_MARKER);

      VirtualFile targetFile = added.getContainingFile().getVirtualFile();
      Document document = documentManager.getDocument(valueExpression.getContainingFile());
      assert document != null;
      document.replaceString(caretOffset, caretOffset + CARET_MARKER.length(), "");
      PsiDocumentManager.getInstance(project).commitDocument(document);

      PsiNavigationSupport.getInstance().createNavigatable(project, targetFile, caretOffset).
        navigate(true);
    }
  }
}
