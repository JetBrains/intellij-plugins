package com.intellij.tapestry.psi;

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.tapestry.lang.TelFileType;
import com.intellij.tapestry.lang.TelLanguage;

/**
 * @author Alexey Chmutov
 *         Date: Jun 22, 2009
 *         Time: 3:11:01 PM
 */
public abstract class TelElementTypes {
  public static final IFileElementType TEL_FILE = new IFileElementType(Language.findInstance(TelLanguage.class));

  public static final IElementType TAP5_EL_START = new TelElementType("TAP5_EL_START");
  public static final IElementType TAP5_EL_END = new TelElementType("TAP5_EL_END");
  public static final IElementType TAP5_EL_IDENTIFIER = new TelElementType("TAP5_EL_IDENTIFIER");
  public static final IElementType TAP5_EL_DOT = new TelElementType("TAP5_EL_DOT");
  public static final IElementType TAP5_EL_COLON = new TelElementType("TAP5_EL_COLON");
  public static final IElementType TAP5_EL_COMMA = new TelElementType("TAP5_EL_COMMA");
  public static final IElementType TAP5_EL_BAD_CHAR = new TelElementType("TAP5_EL_BAD_CHAR");

  static final IElementType TAP5_EL_CONTENT = new TelElementType("TAP5_EL_CONTENT");
  static final Key<ASTNode> TAP5_CONTEXT_NODE_KEY = Key.create("TAP5_CONTEXT_NODE_KEY");
  static final ILazyParseableElementType TAP5_EL_HOLDER = new ILazyParseableElementType("TAP5_EL_HOLDER", TelFileType.INSTANCE.getLanguage()) {
    public ASTNode parseContents(ASTNode chameleon) {
      final Project project = chameleon.getPsi().getProject();
      final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, getLanguage(), chameleon.getText());
      final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(getLanguage()).createParser(project);

      builder.putUserData(TAP5_CONTEXT_NODE_KEY, chameleon.getTreeParent());
      final ASTNode result = parser.parse(this, builder).getFirstChildNode();
      builder.putUserData(TAP5_CONTEXT_NODE_KEY, null);
      return result;
    }
  };
}
