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
package com.intellij.protobuf.ide.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.protobuf.ide.folding.ProtoFoldingUtils.ConsecutiveElementGrouper;
import com.intellij.protobuf.lang.psi.ProtoBlockBody;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** A {@link FoldingBuilderEx} implementation for protobuf and prototext files. */
public class ProtoFoldingBuilder extends FoldingBuilderEx implements DumbAware {
  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(
      @NotNull PsiElement root, @NotNull Document document, boolean quick) {

    Collection<PsiElement> elements =
        PsiTreeUtil.findChildrenOfAnyType(root, ProtoBlockBody.class, PsiComment.class);
    final List<FoldingDescriptor> descriptors = new ArrayList<>(elements.size());

    ConsecutiveElementGrouper grouper = new ConsecutiveElementGrouper();
    for (PsiElement element : elements) {
      if (element instanceof ProtoBlockBody) {
        ProtoFoldingUtils.addIfNotNull(descriptors, buildBlockDescriptor((ProtoBlockBody) element));
      } else if (ProtoTokenTypes.BLOCK_COMMENT.equals(element.getNode().getElementType())) {
        descriptors.add(new FoldingDescriptor(element.getNode(), element.getTextRange()));
      } else if (ProtoTokenTypes.LINE_COMMENT.equals(element.getNode().getElementType())
          && ProtoFoldingUtils.isOnOwnLine(element, document)) {
        ProtoFoldingUtils.addIfNotNull(descriptors, grouper.pushElement(element));
      }
    }
    ProtoFoldingUtils.addIfNotNull(descriptors, grouper.buildBlock());

    return descriptors.toArray(new FoldingDescriptor[0]);
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    if (ProtoTokenTypes.BLOCK_COMMENT.equals(node.getElementType())) {
      return "/*...*/";
    } else if (ProtoTokenTypes.LINE_COMMENT.equals(node.getElementType())) {
      if (node.getChars().charAt(0) == '#') {
        return "#...";
      }
      return "//...";
    }

    // Block body placeholder text is pre-calculated.
    return null;
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    return false;
  }

  private static FoldingDescriptor buildBlockDescriptor(ProtoBlockBody block) {
    PsiElement open = block.getStart();
    PsiElement close = block.getEnd();
    if (close != null) {
      String text = open.getText() + "..." + close.getText();
      return new FoldingDescriptor(
          block.getNode(),
          new TextRange(
              open.getNode().getStartOffset(),
              close.getNode().getStartOffset() + close.getTextLength()),
          /* group= */ null,
          text);
    }
    return null;
  }
}
