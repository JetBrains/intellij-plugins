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

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/** A common interface implemented by all prototext elements. */
public interface PbTextElement extends PsiElement {

  /** Returns the {@link PbTextRootMessage} in the tree above this text element. */
  default PbTextRootMessage getRootMessage() {
    return PsiTreeUtil.getParentOfType(this, PbTextRootMessage.class);
  }
}
