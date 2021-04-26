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

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/** An element that wraps a {@link ProtoSymbolPath} specifying scoping and resolution behavior. */
public interface ProtoSymbolPathContainer extends PsiElement {

  Key<ProtoSymbolPathDelegate> DELEGATE_OVERRIDE = Key.create("SYMBOL_PATH_DELEGATE_OVERRIDE");

  /** Returns the {@link ProtoSymbolPathDelegate delegate} for this container. */
  default ProtoSymbolPathDelegate getPathDelegate() {
    ProtoSymbolPathDelegate override = getUserData(DELEGATE_OVERRIDE);
    return override != null ? override : getDefaultPathDelegate();
  }

  /** Sets a {@link ProtoSymbolPathDelegate delegate} override for this container. */
  default void setDelegateOverride(ProtoSymbolPathDelegate override) {
    putUserData(DELEGATE_OVERRIDE, override);
  }

  /** Return the default {@link ProtoSymbolPathDelegate delegate} for this container. */
  @NotNull
  ProtoSymbolPathDelegate getDefaultPathDelegate();
}
