package com.intellij.tapestry.psi;

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;

/**
 * @author Alexey Chmutov
 *         Date: Jun 22, 2009
 *         Time: 3:11:01 PM
 */
public interface ELTokenType {
  Key<ASTNode> ourContextNodeKey = Key.create("Tap5.EL.context.node");
  IElementType TAP5_EL_CONTENT = new IElementType("TAP5_EL_CONTENT", null);

  ILazyParseableElementType TAP5_EL_HOLDER = new ILazyParseableElementType("EL_HOLDER") {
    public ASTNode parseContents(ASTNode chameleon) {
      final Project project = chameleon.getPsi().getProject();
      final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, getLanguage(), chameleon.getText());
      final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(getLanguage()).createParser(project);

      builder.putUserData(ourContextNodeKey, chameleon.getTreeParent());
      final ASTNode result = parser.parse(this, builder).getFirstChildNode();
      builder.putUserData(ourContextNodeKey, null);
      return result;
    }
  };
}
