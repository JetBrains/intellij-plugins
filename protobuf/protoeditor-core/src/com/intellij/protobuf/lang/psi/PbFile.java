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

import com.intellij.psi.impl.PsiFileEx;
import com.intellij.psi.impl.source.PsiFileWithStubSupport;
import com.intellij.psi.util.QualifiedName;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A protobuf file. */
public interface PbFile
    extends PsiFileEx,
        PsiFileWithStubSupport,
        PbOptionStatementOwner,
        PbSymbolOwner,
        PbStatementOwner {

  /** Returns the syntax statement element, or <code>null</code> if one is not specified. */
  @Nullable
  PbSyntaxStatement getSyntaxStatement();

  /** Returns the package statement element, or <code>null</code> if one is not specified. */
  @Nullable
  PbPackageStatement getPackageStatement();

  /** Returns the list of import statement elements. */
  @NotNull
  List<PbImportStatement> getImportStatements();

  /**
   * Returns the {@link SyntaxLevel} for the specified syntax version.
   *
   * <p>If no level is specified, the default proto2 level is returned.
   */
  @NotNull
  SyntaxLevel getSyntaxLevel();

  /** Returns the qualified name declared in the file's package statement. Defaults to "" */
  @NotNull
  QualifiedName getPackageQualifiedName();

  /**
   * Returns a map containing the children of the given file-local package name.
   *
   * <p>For example, if the declared package is "com.foo.bar", and packageName is "com.foo", the
   * result is a single-element map containing "bar". If packageName is "com.foo.bar", the result is
   * the full set of top-level symbols defined in the file.
   *
   * @param packageName The package name.
   * @return A map containing the package's children.
   */
  @NotNull
  Map<String, Collection<PbSymbol>> getPackageSymbolMap(QualifiedName packageName);

  /**
   * Returns a map of all fully-qualified symbols defined in this file.
   *
   * <p>Keys in this map are the value of the symbol's {@link PbSymbol#getQualifiedName()} method.
   * Every symbol is represented, including intermediate types and package symbols. For example, if
   * a message element has the qualified name "com.foo.bar.MyMessage", this map will contain entries
   * for "com", "com.foo", "com.foo.bar", and "com.foo.bar.MyMessage".
   */
  @NotNull
  Map<QualifiedName, Collection<PbSymbol>> getLocalQualifiedSymbolMap();

  /**
   * Returns a map of all fully-qualified symbols exported when this file is imported.
   *
   * <p>Specifically, the result contains the following:
   *
   * <ul>
   *   <li>Entries from this file's {@link #getLocalQualifiedSymbolMap()}
   *   <li>Entries from getExportedQualifiedSymbolMap() for each public import
   * </ul>
   *
   * @see #getLocalQualifiedSymbolMap()
   */
  @NotNull
  Map<QualifiedName, Collection<PbSymbol>> getExportedQualifiedSymbolMap();

  /**
   * Returns a map of all fully-qualified symbols defined in this and imported files.
   *
   * <p>Specifically, the result contains the following:
   *
   * <ul>
   *   <li>Entries from this file's {@link #getLocalQualifiedSymbolMap()}
   *   <li>Entries from getExportedQualifiedSymbolMap() for each import
   * </ul>
   *
   * @see #getLocalQualifiedSymbolMap()
   */
  @NotNull
  Map<QualifiedName, Collection<PbSymbol>> getFullQualifiedSymbolMap();

  /**
   * Returns the {@link PbSymbolOwner} that owns the elements defined in this file. This is either
   * the most-qualified {@link PbPackageName}, or the file itself if no package is defined.
   */
  @NotNull
  PbSymbolOwner getPrimarySymbolOwner();
}
