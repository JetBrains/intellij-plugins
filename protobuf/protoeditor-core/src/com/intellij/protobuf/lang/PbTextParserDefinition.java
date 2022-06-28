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
package com.intellij.protobuf.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.lang.lexer.ProtoLexer;
import com.intellij.protobuf.lang.parser.PbTextParser;
import com.intellij.protobuf.lang.psi.PbTextTypes;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import com.intellij.protobuf.lang.psi.impl.PbTextFileImpl;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/** A {@link ParserDefinition} for prototext files. */
public class PbTextParserDefinition implements ParserDefinition {
  public static final PbTextParserDefinition INSTANCE = new PbTextParserDefinition();

  public static final IFileElementType FILE = new IFileElementType(PbTextLanguage.INSTANCE);

  public PbTextParserDefinition() {}

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return ProtoLexer.forPrototext();
  }

  @Override
  public @NotNull PsiParser createParser(final Project project) {
    return new PbTextParser();
  }

  @NotNull
  @Override
  public TokenSet getWhitespaceTokens() {
    return ProtoTokenTypes.WHITE_SPACE;
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return ProtoTokenTypes.COMMENTS;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return ProtoTokenTypes.STRINGS;
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new PbTextFileImpl(viewProvider, PbTextLanguage.INSTANCE);
  }

  @Override
  public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    return PbTextTypes.Factory.createElement(node);
  }
}
