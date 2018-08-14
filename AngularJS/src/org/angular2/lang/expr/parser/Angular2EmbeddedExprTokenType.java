// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angular2.lang.expr.parser;

import com.intellij.embedding.EmbeddingElementType;
import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.ICustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementTypeBase;
import com.intellij.psi.tree.ILightLazyParseableElementType;
import com.intellij.util.CharTable;
import com.intellij.util.diff.FlyweightCapableTreeStructure;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.lexer.Angular2Lexer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class Angular2EmbeddedExprTokenType extends IElementType implements EmbeddingElementType, ICustomParsingType, ILazyParseableElementTypeBase,
                                                                           ILightLazyParseableElementType {

  public static final Angular2EmbeddedExprTokenType ACTION_EXPR = new Angular2EmbeddedExprTokenType(
    "NG:ACTION_EXPR", Angular2EmbeddedExprTokenType.ExpressionType.ACTION);
  public static final Angular2EmbeddedExprTokenType BINDING_EXPR = new Angular2EmbeddedExprTokenType(
    "NG:BINDING_EXPR", Angular2EmbeddedExprTokenType.ExpressionType.BINDING);
  public static final Angular2EmbeddedExprTokenType INTERPOLATION_EXPR = new Angular2EmbeddedExprTokenType(
    "NG:INTERPOLATION_EXPR", Angular2EmbeddedExprTokenType.ExpressionType.INTERPOLATION);
  public static final Angular2EmbeddedExprTokenType SIMPLE_BINDING_EXPR = new Angular2EmbeddedExprTokenType(
    "NG:SIMPLE_BINDING_EXPR", ExpressionType.SIMPLE_BINDING);

  public static Angular2EmbeddedExprTokenType createTemplateBindings(String templateKey) {
    return new Angular2EmbeddedExprTokenType("NG:TEMPLATE_BINDINGS_EXPR", ExpressionType.TEMPLATE_BINDINGS, templateKey);
  }

  @NotNull private final ExpressionType myExpressionType;
  @Nullable private final String myTemplateKey;

  private Angular2EmbeddedExprTokenType(@NotNull @NonNls String debugName, @NotNull ExpressionType expressionType) {
    super(debugName, Angular2Language.INSTANCE);
    myExpressionType = expressionType;
    myTemplateKey = null;
  }

  private Angular2EmbeddedExprTokenType(@NotNull @NonNls String debugName, @NotNull ExpressionType expressionType, @Nullable @NonNls String templateKey) {
    super(debugName, Angular2Language.INSTANCE, false);
    myExpressionType = expressionType;
    myTemplateKey = templateKey;
  }

  @NotNull
  @Override
  public ASTNode parse(@NotNull CharSequence text, @NotNull CharTable table) {
    return new LazyParseableElement(this, text);
  }

  @Override
  public ASTNode parseContents(@NotNull ASTNode chameleon) {
    PsiBuilder builder = doParseContents(chameleon);
    return builder.getTreeBuilt().getFirstChildNode();
  }

  @Override
  public FlyweightCapableTreeStructure<LighterASTNode> parseContents(LighterLazyParseableNode chameleon) {
    PsiBuilder builder = doParseContents(chameleon);
    return builder.getLightTree();
  }

  protected PsiBuilder doParseContents(@NotNull Object chameleon) {
    assert chameleon instanceof ASTNode || chameleon instanceof LighterLazyParseableNode : chameleon.getClass();

    Project project;
    if (chameleon instanceof ASTNode) {
      PsiElement psi = ((ASTNode)chameleon).getPsi();
      project = psi.getProject();
    }
    else {
      PsiFile file = ((LighterLazyParseableNode)chameleon).getContainingFile();
      assert file != null : "Let's add LighterLazyParseableNode#getProject() method";
      project = file.getProject();
    }

    CharSequence chars = chameleon instanceof ASTNode
                         ? ((ASTNode)chameleon).getChars()
                         : ((LighterLazyParseableNode)chameleon).getText();

    final Lexer lexer = new Angular2Lexer();

    PsiBuilder builder =
      chameleon instanceof ASTNode
      ? PsiBuilderFactory.getInstance().createBuilder(project, (ASTNode)chameleon, lexer, Angular2Language.INSTANCE, chars)
      : PsiBuilderFactory.getInstance().createBuilder(project, (LighterLazyParseableNode)chameleon, lexer, Angular2Language.INSTANCE, chars);

    myExpressionType.parse(builder, this, myTemplateKey);
    return builder;
  }

  public enum ExpressionType {
    ACTION(Angular2Parser::parseAction),
    BINDING(Angular2Parser::parseBinding),
    INTERPOLATION(Angular2Parser::parseInterpolation),
    SIMPLE_BINDING(Angular2Parser::parseSimpleBinding),
    TEMPLATE_BINDINGS(null);

    private final BiConsumer<Angular2Parser, IElementType> myParseMethod;

    ExpressionType(BiConsumer<Angular2Parser, IElementType> parseMethod) {
      myParseMethod = parseMethod;
    }

    public void parse(@NotNull PsiBuilder builder, @NotNull IElementType root, @Nullable String templateKey) {
      Angular2Parser parser = new Angular2Parser(builder);
      if (this == TEMPLATE_BINDINGS) {
        assert templateKey != null;
        parser.parseTemplateBindings(root, templateKey);
      } else {
        myParseMethod.accept(parser, root);
      }
    }

  }

}