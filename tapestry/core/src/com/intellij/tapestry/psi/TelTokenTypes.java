package com.intellij.tapestry.psi;

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.tapestry.lang.TelFileType;
import com.intellij.tapestry.lang.TelLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class TelTokenTypes {
  public static final IFileElementType TEL_FILE = new IFileElementType(TelLanguage.INSTANCE);

  public static final TelTokenType TAP5_EL_START = new TelTokenType("${");
  public static final TelTokenType TAP5_EL_END = new TelTokenType("}");
  public static final TelTokenType TAP5_EL_IDENTIFIER = new TelTokenType("TAP5_EL_IDENTIFIER");
  public static final TelTokenType TAP5_EL_DOT = new TelTokenType(".");
  public static final TelTokenType TAP5_EL_COLON = new TelTokenType(":");
  public static final TelTokenType TAP5_EL_COMMA = new TelTokenType(",");
  public static final TelTokenType TAP5_EL_QUESTION_DOT = new TelTokenType("?.");
  public static final TelTokenType TAP5_EL_RANGE = new TelTokenType("..");
  public static final TelTokenType TAP5_EL_EXCLAMATION = new TelTokenType("!");
  public static final TelTokenType TAP5_EL_LEFT_PARENTH = new TelTokenType("(");
  public static final TelTokenType TAP5_EL_RIGHT_PARENTH = new TelTokenType(")");
  public static final TelTokenType TAP5_EL_LEFT_BRACKET = new TelTokenType("[");
  public static final TelTokenType TAP5_EL_RIGHT_BRACKET = new TelTokenType("]");
  public static final TelTokenType TAP5_EL_STRING = new TelTokenType("TAP5_EL_STRING");
  public static final TelTokenType TAP5_EL_INTEGER = new TelTokenType("TAP5_EL_INTEGER");
  public static final TelTokenType TAP5_EL_DECIMAL = new TelTokenType("TAP5_EL_DECIMAL");
  public static final TelTokenType TAP5_EL_BOOLEAN = new TelTokenType("TAP5_EL_BOOLEAN");
  public static final TelTokenType TAP5_EL_NULL = new TelTokenType("TAP5_EL_NULL");

  public static final IElementType TAP5_EL_BAD_CHAR = new TelTokenType("TAP5_EL_BAD_CHAR");

  static final IElementType TAP5_EL_CONTENT = new TelTokenType("TAP5_EL_CONTENT");
  static final Key<ASTNode> TAP5_CONTEXT_NODE_KEY = Key.create("TAP5_CONTEXT_NODE_KEY");
  public static final ILazyParseableElementType TAP5_EL_HOLDER =
      new ILazyParseableElementType("TAP5_EL_HOLDER", TelFileType.INSTANCE.getLanguage()) {
        @Override
        public ASTNode parseContents(@NotNull ASTNode chameleon) {
          final Project project = chameleon.getPsi().getProject();
          final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon);
          final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(getLanguage()).createParser(project);

          builder.putUserData(TAP5_CONTEXT_NODE_KEY, chameleon.getTreeParent());
          final ASTNode result = parser.parse(this, builder).getFirstChildNode();
          builder.putUserData(TAP5_CONTEXT_NODE_KEY, null);
          return result;
        }
      };

  public static final TokenSet WHITESPACES = TokenSet.create(TokenType.WHITE_SPACE);
  public static final TokenSet STRING_LITERALS = TokenSet.create(TAP5_EL_STRING);
}
