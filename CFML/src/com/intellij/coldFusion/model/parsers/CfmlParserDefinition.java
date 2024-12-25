// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.parsers;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.CfmlCompositeElementType;
import com.intellij.coldFusion.model.psi.impl.CfmlComponentImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagComponentImpl;
import com.intellij.coldFusion.model.psi.stubs.CfmlStubElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmlParserDefinition implements ParserDefinition {

  @Override
  public @NotNull Lexer createLexer(Project project) {
    return new CfmlLexer(false, project);
  }

  @Override
  public @NotNull PsiParser createParser(Project project) {
    return new CfmlParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return CfmlStubElementTypes.CFML_FILE;
  }

  @Override
  public @NotNull TokenSet getWhitespaceTokens() {
    return TokenSet.create(CfmlTokenTypes.WHITE_SPACE, CfscriptTokenTypes.WHITE_SPACE);
  }

  @Override
  public @NotNull TokenSet getCommentTokens() {
    return TokenSet.create(CfmlTokenTypes.COMMENT, CfscriptTokenTypes.COMMENT, CfmlTokenTypes.VAR_ANNOTATION);
  }

  @Override
  public @NotNull TokenSet getStringLiteralElements() {
    return TokenSet.create(CfmlTokenTypes.STRING_TEXT,
                           CfmlTokenTypes.SINGLE_QUOTE, CfmlTokenTypes.DOUBLE_QUOTE,
                           CfmlTokenTypes.SINGLE_QUOTE_CLOSER, CfmlTokenTypes.DOUBLE_QUOTE_CLOSER,
                /*CfmlTokenTypes.STRING_TEXT,*/
                CfmlTokenTypes.SINGLE_QUOTE, CfmlTokenTypes.SINGLE_QUOTE_CLOSER,
                CfmlTokenTypes.DOUBLE_QUOTE, CfmlTokenTypes.DOUBLE_QUOTE_CLOSER);
  }

  @Override
  public @NotNull PsiElement createElement(ASTNode node) {
    final IElementType type = node.getElementType();

    if (type instanceof CfmlCompositeElementType) {
      return ((CfmlCompositeElementType)type).createPsiElement(node);
    }
    else if (type == CfmlStubElementTypes.COMPONENT_DEFINITION) {
      return new CfmlComponentImpl(node);
    }
    else if (type == CfmlStubElementTypes.COMPONENT_TAG) {
      return new CfmlTagComponentImpl(node);
    }
    throw new AssertionError("Unknown type: " + type);


        /*if (type == CfmlElementTypes.FUNCTION_CALL_NAME) {
            return new CfmlFunctionCallExpression(node, false);
            // return new CfmlReferenceExpression(node);
        } else if (type == CfmlElementTypes.FUNCTION_DEFINITION_NAME) {
            return new CfmlFunctionImpl(node, false);
            // return new CfmlDefinitionExpression(node);
        } else if (type == CfscriptElementTypes.FUNCTION_CALL_NAME) {
            return new CfmlFunctionCallExpression(node, true);
            // return new CfmlReferenceExpression(node);
        } else if (type == CfscriptElementTypes.FUNCTION_DEFINITION_NAME) {
            return new CfmlFunctionImpl(node, true);
            //return new CfmlDefinitionExpression(node);
        } else if (type == CfscriptElementTypes.FUNCTION_DEFINITION) {
            return new CfscriptFunction(node);
        } else if (type == CfmlElementTypes.NAMED_ATTRIBUTE) {
            return new CfmlVariableDefinition(node, false);
        } else if (type == CfmlElementTypes.REFERENCE) {
            return new CfmlVariableUsing(node, true);
        } else if (type == CfscriptElementTypes.VAR_DEF) {
            return new CfmlVariableDefinition(node, true);
        } else if (type == CfmlElementTypes.ARGUMENT_LIST) {
            return new CfmlArgumentList(node);
        }
        */
    // return new CfmlElementImpl(node);
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new CfmlFile(viewProvider, CfmlLanguage.INSTANCE);
  }

  @Override
  public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }

  @Override
  public String toString() {
    return "CfmlParserDefinition";
  }
}

