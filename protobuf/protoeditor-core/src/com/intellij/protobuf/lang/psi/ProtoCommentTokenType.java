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
package com.intellij.protobuf.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILeafElementType;
import com.intellij.protobuf.lang.ProtoBaseLanguage;
import org.jetbrains.annotations.NotNull;

/** An {@link ILeafElementType} that returns {@link ProtoComment} instances. */
public class ProtoCommentTokenType extends IElementType implements ILeafElementType {
  ProtoCommentTokenType(String debugName) {
    super(debugName, ProtoBaseLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public ASTNode createLeafNode(@NotNull CharSequence leafText) {
    return new ProtoComment(this, leafText);
  }
}
