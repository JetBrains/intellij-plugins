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

import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A statement is a primary lexical entity in a proto file ending with a semicolon or a closing
 * curly brace. For example, an import statement or a message definition.
 *
 * <p>{@link PbDefinition} objects are statements.
 */
public interface PbStatement extends PbElement, PbCommentOwner {

  @Nullable
  default PbStatementOwner getStatementOwner() {
    return PbPsiImplUtil.getStatementOwner(this);
  }

  @NotNull
  default String getPresentableText() {
    return getText();
  }
}
