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
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.thoughtworks.gauge.language.token.SpecTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class SpecFoldingBuilder extends GaugeFoldingBuilder {
  @Override
  public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull ASTNode astNode, @NotNull Document document) {
    List<FoldingDescriptor> descriptors = new ArrayList<>();
    addNodes(astNode, descriptors, SpecTokenTypes.SPEC_DETAIL, SpecTokenTypes.SPEC_HEADING);
    addNodes(astNode, descriptors, SpecTokenTypes.SCENARIO, SpecTokenTypes.SCENARIO_HEADING);
    addNodes(astNode, descriptors, SpecTokenTypes.TEARDOWN, SpecTokenTypes.TEARDOWN_IDENTIFIER);
    return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
  }

  private static void addNodes(@NotNull ASTNode astNode, List<FoldingDescriptor> descriptors, IElementType pNode, IElementType cNode) {
    for (ASTNode node : astNode.getChildren(TokenSet.create(pNode))) {
      addNode(descriptors, node, node.findChildByType(cNode));
    }
  }
}
