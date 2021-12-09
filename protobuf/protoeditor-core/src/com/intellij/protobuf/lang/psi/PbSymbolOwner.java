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

import com.google.common.collect.Multimap;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * A PbSymbolOwner is an element that can enumerate {@link PbSymbol} children. Many symbol owners
 * are also {@link PbStatementOwner}s, but not all. For example, a {@link PbOneofDefinition} is a
 * StatementOwner, but since it does not define its own child scope, it's not a symbol owner.
 */
public interface PbSymbolOwner extends PbElement {
  /**
   * Returns the scope that child statements reside in. Some statement owners, such as messages,
   * create new scopes. Others, such as oneof definitions, do not. In the latter case, this method
   * returns the parent scope. The scope of a PbFile is the defined package statement.
   */
  @Nullable
  QualifiedName getChildScope();

  /**
   * Returns a mapping of child symbol names to values. Names are relative to the value of {@link
   * #getChildScope()}.
   */
  @NotNull
  Multimap<String, PbSymbol> getSymbolMap();

  @NotNull
  default Collection<PbSymbol> getSymbols() {
    return getSymbolMap().values();
  }

  @NotNull
  default <T> Collection<T> getSymbols(Class<T> type) {
    return getSymbols()
        .stream()
        .filter(type::isInstance)
        .map(type::cast)
        .collect(Collectors.toList());
  }

  /**
   * Return symbols matching the given relative descendant name.
   *
   * <p>For example, if this element's {@link #getChildScope() child scope} is "com.foo.Msg", and
   * <code>relativeName</code> is "SubMessage.SubMessageEnum", any returned symbols will have a
   * qualified name of "com.foo.Msg.SubMessage.SubMessageEnum".
   *
   * @param relativeName the relative descendant name
   * @return a collection of matching symbols
   */
  @NotNull
  default Collection<PbSymbol> findSymbols(QualifiedName relativeName) {
    QualifiedName childScope = getChildScope();
    if (childScope == null || relativeName == null) {
      return Collections.emptyList();
    }
    QualifiedName absoluteName = childScope.append(relativeName);
    return getPbFile().getLocalQualifiedSymbolMap().get(absoluteName);
  }

  /**
   * Return symbols matching the given relative descendant name, filtered to the given type.
   *
   * @see #findSymbols(QualifiedName)
   */
  @NotNull
  default <T> Collection<T> findSymbols(QualifiedName relativeName, Class<T> type) {
    return findSymbols(relativeName)
        .stream()
        .filter(type::isInstance)
        .map(type::cast)
        .collect(Collectors.toList());
  }

  /** Return direct symbol children with the given name. */
  @NotNull
  default Collection<PbSymbol> findSymbols(String name) {
    return getSymbolMap().get(name);
  }

  /** Return direct symbol children with the given name and type. */
  @NotNull
  default <T> Collection<T> findSymbols(String name, Class<T> type) {
    return findSymbols(name)
        .stream()
        .filter(type::isInstance)
        .map(type::cast)
        .collect(Collectors.toList());
  }
}
