/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.tree.TokenSet;
import com.thoughtworks.gauge.language.token.ConceptTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class ConceptFoldingBuilder extends GaugeFoldingBuilder {
  @Override
  public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull ASTNode astNode, @NotNull Document document) {
    List<FoldingDescriptor> descriptors = new ArrayList<>();
    for (ASTNode node : astNode.getChildren(TokenSet.create(ConceptTokenTypes.CONCEPT))) {
      addNode(descriptors, node, node.findChildByType(ConceptTokenTypes.CONCEPT_HEADING));
    }
    return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
  }
}
