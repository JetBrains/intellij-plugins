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
package com.intellij.protobuf.lang.findusages;

import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.protobuf.ide.PbIdeBundle;
import com.intellij.protobuf.lang.PbParserDefinition;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

/** Helps find usages (basic support for usages within .proto files). */
public class PbFindUsagesProvider implements FindUsagesProvider {
  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return psiElement instanceof PbSymbol;
  }

  @Override
  public @NotNull String getHelpId(@NotNull PsiElement psiElement) {
    return HelpID.FIND_OTHER_USAGES;
  }

  @Override
  public @NotNull String getType(@NotNull PsiElement element) {
    if (element instanceof PbFile) {
      return PbIdeBundle.message("proto.type.file");
    } else if (element instanceof PbPackageName) {
      return PbIdeBundle.message("proto.type.package");
    } else if (element instanceof PbGroupDefinition) {
      return PbIdeBundle.message("proto.type.group");
    } else if (element instanceof PbField) {
      return PbIdeBundle.message("proto.type.field");
    } else if (element instanceof PbMessageType) {
      return PbIdeBundle.message("proto.type.message");
    } else if (element instanceof PbEnumDefinition) {
      return PbIdeBundle.message("proto.type.enum");
    } else if (element instanceof PbEnumValue) {
      return PbIdeBundle.message("proto.type.enum.value");
    } else if (element instanceof PbOneofDefinition) {
      return PbIdeBundle.message("proto.type.oneof");
    } else if (element instanceof PbServiceDefinition) {
      return PbIdeBundle.message("proto.type.service");
    } else if (element instanceof PbServiceMethod) {
      return PbIdeBundle.message("proto.type.method");
    } else if (element instanceof PbServiceStream) {
      return PbIdeBundle.message("proto.type.stream");
    }
    return PbIdeBundle.message("proto.type.unknown");
  }

  @Override
  public @NotNull String getDescriptiveName(@NotNull PsiElement psiElement) {
    return getNodeText(psiElement, true);
  }

  @SuppressWarnings("HardCodedStringLiteral")
  @Override
  public @NotNull String getNodeText(@NotNull PsiElement psiElement, boolean useFullName) {
    PbSymbol symbol = ObjectUtils.tryCast(psiElement, PbSymbol.class);
    if (symbol != null) {
      if (useFullName) {
        QualifiedName qualifiedName = symbol.getQualifiedName();
        if (qualifiedName != null) {
          return qualifiedName.toString();
        }
      }
      if (symbol.getName() != null) {
        return symbol.getName();
      }
    }
    return psiElement.toString();
  }

  @Override
  public @NotNull WordsScanner getWordsScanner() {
    PbParserDefinition parserDefinition = new PbParserDefinition();
    return new DefaultWordsScanner(
        parserDefinition.createLexer(null),
        TokenSet.create(ProtoTokenTypes.IDENTIFIER_LITERAL),
        parserDefinition.getCommentTokens(),
        TokenSet.create(ProtoTokenTypes.STRING_LITERAL));
  }
}
