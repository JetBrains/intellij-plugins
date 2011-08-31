package com.google.jstestdriver.idea.assertFramework;

import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsGeneratorUtils {

  private JsGeneratorUtils() {
  }

  public static void generateProperty(
      @NotNull JSObjectLiteralExpression objectLiteralExpression,
      @NotNull GenerateActionContext context,
      @NotNull String markedPropertyStr
  ) {
    PsiElement precedingAnchor = objectLiteralExpression.getFirstChild();
    if (precedingAnchor == null) {
      return;
    }
    final int caretOffset = context.getCaretOffsetInDocument();
    JSProperty precedingProperty = findPrecedingProperty(objectLiteralExpression, caretOffset);
    JSProperty followingProperty = findFollowingProperty(objectLiteralExpression, caretOffset);
    boolean generateCommaBefore = false;
    boolean generateCommaAfter = followingProperty != null;
    if (precedingProperty != null) {
      precedingAnchor = precedingProperty;
      PsiElement comma = findNextSiblingComma(precedingProperty);
      generateCommaBefore = comma == null;
      if (comma != null) {
        precedingAnchor = comma;
      }
    }
    TextRange whitespaceTextRange = unionFollowingWhitespaceTextRanges(precedingAnchor);
    generateProperty(context, markedPropertyStr, whitespaceTextRange, generateCommaBefore, generateCommaAfter);
  }

  private static void generateProperty(
      @NotNull GenerateActionContext context,
      @NotNull String markedPropertyStr,
      @NotNull TextRange whitespaceTextRange,
      boolean commaBeforeRequired,
      boolean commaAfterRequired
  ) {
    final int caretOffset = context.getCaretOffsetInDocument();
    final boolean insideWhitespaceArea = whitespaceTextRange.contains(caretOffset);
    int moveCaretToOffset = insideWhitespaceArea ? caretOffset : whitespaceTextRange.getStartOffset();
    if (commaBeforeRequired) {
      generateCommaAt(context, whitespaceTextRange.getStartOffset());
      moveCaretToOffset++;
    }
    context.getCaretModel().moveToOffset(moveCaretToOffset);
    LineRange whitespaceLineRange = createLineRangeByTextRange(context, whitespaceTextRange);
    int caretLineNumber = getLineNumberAtOffset(context, moveCaretToOffset);
    String leadingNewLine = "";
    if (caretLineNumber == whitespaceLineRange.getStartLine()) {
      leadingNewLine = "\n";
    }
    Template template = createDefaultTemplate(leadingNewLine + markedPropertyStr);
    if (commaAfterRequired) {
      template.addTextSegment(",");
    }
    if (whitespaceLineRange.getStartLine() == whitespaceLineRange.getEndLine()) {
      template.addTextSegment("\n");
    }
    context.startTemplate(template);
  }

  private static void generateCommaAt(@NotNull GenerateActionContext context, final int offset) {
    context.getCaretModel().moveToOffset(offset);
    Template template = createDefaultTemplate(",");
    context.startTemplate(template);
  }

  @Nullable
  private static JSProperty findPrecedingProperty(@NotNull JSObjectLiteralExpression objectLiteralExpression, int caretOffset) {
    JSProperty[] properties = JsPsiUtils.getProperties(objectLiteralExpression);
    JSProperty preceding = null;
    for (JSProperty currentProperty : properties) {
      int endOffset = currentProperty.getTextRange().getEndOffset();
      if (currentProperty.getTextRange().getStartOffset() < caretOffset && caretOffset < endOffset) {
        return currentProperty;
      }
      if (endOffset <= caretOffset) {
        if (preceding == null || preceding.getTextRange().getEndOffset() < endOffset) {
          preceding = currentProperty;
        }
      }
    }
    return preceding;
  }

  @Nullable
  private static JSProperty findFollowingProperty(@NotNull JSObjectLiteralExpression objectLiteralExpression, int caretOffset) {
    JSProperty[] properties = JsPsiUtils.getProperties(objectLiteralExpression);
    JSProperty following = null;
    for (JSProperty property : properties) {
      int startOffset = property.getTextRange().getStartOffset();
      if (caretOffset <= startOffset) {
        if (following == null || startOffset < following.getTextRange().getStartOffset()) {
          following = property;
        }
      }
    }
    return following;
  }

  @Nullable
  public static PsiElement findNextSiblingComma(@NotNull PsiElement precedingAnchor) {
    PsiElement next = precedingAnchor.getNextSibling();
    while (next instanceof ASTNode) {
      ASTNode node = (ASTNode) next;
      IElementType elementType = node.getElementType();
      if (elementType == JSTokenTypes.COMMA) {
        return next;
      } else if (elementType != JSTokenTypes.WHITE_SPACE) {
        break;
      }
      next = next.getNextSibling();
    }
    return null;
  }

  @NotNull
  private static TextRange unionFollowingWhitespaceTextRanges(@NotNull final PsiElement element) {
    int startOffset = element.getTextRange().getEndOffset();
    int endOffset = startOffset;
    PsiElement e = element.getNextSibling();
    while (e instanceof ASTNode) {
      ASTNode node = (ASTNode) e;
      if (node.getElementType() == JSTokenTypes.WHITE_SPACE) {
        endOffset = node.getTextRange().getEndOffset();
        e = e.getNextSibling();
      } else {
        break;
      }
    }
    return TextRange.create(startOffset, endOffset);
  }

  public static void generateObjectLiteralWithPropertyAsArgument(
      @NotNull GenerateActionContext context,
      @NotNull String markedPropertyStr,
      @NotNull JSArgumentList argumentList,
      int addAtPosition
  ) {
    JSExpression[] expressions = JsPsiUtils.getArguments(argumentList);
    if (expressions.length < addAtPosition) {
      return;
    }
    PsiElement precedingElement = addAtPosition == 0 ? argumentList.getFirstChild() : expressions[addAtPosition - 1];
    if (precedingElement == null) {
      return;
    }
    PsiElement comma = findNextSiblingComma(precedingElement);
    if (comma != null) {
      precedingElement = comma;
    }
    context.getCaretModel().moveToOffset(precedingElement.getTextRange().getEndOffset());

    String leadingPrefix = comma == null && addAtPosition != 0 ? "," : "";
    Template template = createDefaultTemplate(leadingPrefix + markedPropertyStr);
    context.startTemplate(template);
  }

  public static Template createDefaultTemplate(@Nullable String markedText) {
    Template template = new TemplateImpl("", "");
    template.setToIndent(true);
    template.setToReformat(true);
    template.setToShortenLongNames(false);
    template.setInline(false);

    fillTemplateWithMarkedText(template, markedText);

    return template;
  }

  public static void fillTemplateWithMarkedText(@NotNull Template template, @Nullable String markedText) {
    if (markedText == null) {
      return;
    }
    Pattern p = Pattern.compile("\\$\\{(.+?)\\}");
    Matcher m = p.matcher(markedText);

    int startInd = 0;
    do {
      boolean variableFound = m.find();
      final String plainText;
      if (variableFound) {
        plainText = markedText.substring(startInd, m.start());
      } else {
        plainText = markedText.substring(startInd);
      }
      fillTemplateWithPlainText(template, plainText);
      if (variableFound) {
        String variableName = m.group(1);
        template.addVariable(
            variableName.replaceAll(" ", "_"),
            new ConstantNode(variableName),
            new ConstantNode(variableName),
            true
        );
        startInd = m.end();
      } else {
        startInd = markedText.length();
      }
    } while (startInd < markedText.length());
  }

  private static void fillTemplateWithPlainText(Template template, String plaintText) {
    int startInd = 0;
    do {
      int caretIndex = plaintText.indexOf('|', startInd);
      int endInd = caretIndex >= 0 ? caretIndex : plaintText.length();
      String txt = plaintText.substring(startInd, endInd);
      if (txt.length() > 0) {
        template.addTextSegment(txt);
      }
      if (caretIndex >= 0) {
        template.addEndVariable();
      }
      startInd = endInd + 1;
    } while (startInd < plaintText.length());
  }

  private static int getLineNumberAtOffset(GenerateActionContext context, int offset) {
    return context.getDocument().getLineNumber(offset);
  }

  private static LineRange createLineRangeByTextRange(GenerateActionContext context, TextRange textRange) {
    return new LineRange(
        getLineNumberAtOffset(context, textRange.getStartOffset()),
        getLineNumberAtOffset(context, textRange.getEndOffset())
    );
  }

  private static class LineRange {
    private final int myStartLine;
    private final int myEndLine;

    private LineRange(int startLine, int endLine) {
      myStartLine = startLine;
      myEndLine = endLine;
    }

    public int getStartLine() {
      return myStartLine;
    }

    public int getEndLine() {
      return myEndLine;
    }
  }
}
