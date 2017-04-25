package com.jetbrains.lang.dart.ide.editor;

import com.intellij.formatting.Indent;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.source.codeStyle.SemanticEditorPosition;
import com.intellij.psi.impl.source.codeStyle.lineIndent.IndentCalculator;
import com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.HashMap;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.impl.source.codeStyle.lineIndent.JavaLikeLangLineIndentProvider.JavaLikeElement.*;

public class DartLineIndentProvider extends JavaLikeLangLineIndentProvider {

  static HashMap<IElementType, SemanticEditorPosition.SyntaxElement> SYNTAX_MAP = new HashMap<>();

  static {
    SYNTAX_MAP.put(DartTokenTypesSets.WHITE_SPACE, Whitespace);
    SYNTAX_MAP.put(DartTokenTypes.SEMICOLON, Semicolon);
    SYNTAX_MAP.put(DartTokenTypes.LBRACE, BlockOpeningBrace);
    SYNTAX_MAP.put(DartTokenTypes.RBRACE, BlockClosingBrace);
    SYNTAX_MAP.put(DartTokenTypes.LBRACKET, ArrayOpeningBracket);
    SYNTAX_MAP.put(DartTokenTypes.RBRACKET, ArrayClosingBracket);
    SYNTAX_MAP.put(DartTokenTypes.LPAREN, LeftParenthesis);
    SYNTAX_MAP.put(DartTokenTypes.RPAREN, RightParenthesis);
    SYNTAX_MAP.put(DartTokenTypes.COLON, Colon);
    SYNTAX_MAP.put(DartTokenTypes.CASE, SwitchCase);
    SYNTAX_MAP.put(DartTokenTypes.DEFAULT, SwitchDefault);
    SYNTAX_MAP.put(DartTokenTypes.IF, IfKeyword);
    SYNTAX_MAP.put(DartTokenTypes.ELSE, ElseKeyword);
    SYNTAX_MAP.put(DartTokenTypes.FOR, ForKeyword);
    SYNTAX_MAP.put(DartTokenTypes.DO, DoKeyword);
    SYNTAX_MAP.put(DartTokenTypesSets.MULTI_LINE_COMMENT, BlockComment);
    SYNTAX_MAP.put(DartTokenTypesSets.MULTI_LINE_COMMENT_END, DocBlockEnd);
    SYNTAX_MAP.put(DartTokenTypesSets.MULTI_LINE_DOC_COMMENT_START, DocBlockStart);
    SYNTAX_MAP.put(DartTokenTypes.COMMA, Comma);
    SYNTAX_MAP.put(DartTokenTypesSets.SINGLE_LINE_COMMENT, LineComment);
    SYNTAX_MAP.put(DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT, LineComment);
  }

  @Nullable
  @Override
  protected SemanticEditorPosition.SyntaxElement mapType(@NotNull IElementType tokenType) {
    return SYNTAX_MAP.get(tokenType);
  }

  @Override
  public boolean isSuitableForLanguage(@NotNull Language language) {
    return language.is(DartLanguage.INSTANCE);
  }

  @Override
  protected Indent.Type getIndentTypeInBrackets() {
    return Indent.Type.NORMAL;
  }

  @Nullable
  @Override
  protected IndentCalculator getIndent(@NotNull Project project, @NotNull Editor editor, @Nullable Language language, int offset) {
    if (getPosition(editor, offset).matchesRule(position -> position.before().isAt(LeftParenthesis))) {
      // Need to skip a common logic and force formatter-based line indent provider to work, because
      // there may be different indents inside parentheses, see https://github.com/dart-lang/dart_style/issues/551
      return null;
    }
    return super.getIndent(project, editor, language, offset);
  }
}
