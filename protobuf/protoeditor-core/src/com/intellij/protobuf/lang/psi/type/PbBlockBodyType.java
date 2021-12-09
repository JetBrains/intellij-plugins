/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.psi.type;

import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.lang.PbParserDefinition;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IErrorCounterReparseableElementType;
import com.intellij.psi.tree.ILightLazyParseableElementType;
import com.intellij.util.diff.FlyweightCapableTreeStructure;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A reparseable protobuf block element. */
class PbBlockBodyType extends IErrorCounterReparseableElementType
    implements ILightLazyParseableElementType {

  PbBlockBodyType(@NonNls final String debugName, final Language language) {
    super(debugName, language);
  }

  @Override
  public ASTNode parseContents(final @NotNull ASTNode chameleon) {
    ParserDefinition parserDefinition = new PbParserDefinition();
    PsiElement psi = chameleon.getPsi();
    assert psi != null : chameleon;
    Project project = psi.getProject();
    PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon);
    LightPsiParser parser = (LightPsiParser)parserDefinition.createParser(project);
    parser.parseLight(this, builder);
    return builder.getTreeBuilt();
  }

  @Override
  public FlyweightCapableTreeStructure<LighterASTNode> parseContents(
      final LighterLazyParseableNode chameleon) {
    ParserDefinition parserDefinition = new PbParserDefinition();
    PsiElement psi = chameleon.getContainingFile();
    assert psi != null : chameleon;
    Project project = psi.getProject();
    PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon);
    LightPsiParser parser = (LightPsiParser) parserDefinition.createParser(project);
    parser.parseLight(this, builder);
    return builder.getLightTree();
  }

  /** Returns the number of unmatched braces found within the input. */
  @Override
  public int getErrorsCount(CharSequence seq, Language fileLanguage, Project project) {
    Lexer lexer = new PbParserDefinition().createLexer(project);
    lexer.start(seq);
    if (lexer.getTokenType() != ProtoTokenTypes.LBRACE) {
      return IErrorCounterReparseableElementType.FATAL_ERROR;
    }
    lexer.advance();
    int balance = 1;
    IElementType type = lexer.getTokenType();
    while (type != null) {
      if (balance == 0) {
        return IErrorCounterReparseableElementType.FATAL_ERROR;
      }
      if (type == ProtoTokenTypes.LBRACE) {
        balance++;
      } else if (type == ProtoTokenTypes.RBRACE) {
        balance--;
      }
      lexer.advance();
      type = lexer.getTokenType();
    }
    return balance;
  }

  @Nullable
  @Override
  public ASTNode createNode(CharSequence text) {
    return new LazyParseableElement(this, text);
  }
}
