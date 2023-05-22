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
package com.intellij.protobuf.lang.psi.impl;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.protobuf.ide.PbCompositeModificationTracker;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.protobuf.lang.PbLanguage;
import com.intellij.protobuf.lang.descriptor.Descriptor;
import com.intellij.protobuf.lang.descriptor.DescriptorOptionType;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PbFileImpl extends PsiFileBase implements PbFile {

  private static final Logger logger = Logger.getInstance(PbFileImpl.class);

  public PbFileImpl(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, PbLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return PbFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "Protocol Buffer File";
  }

  @Override
  @Nullable
  public PbSyntaxStatement getSyntaxStatement() {
    return PsiTreeUtil.getChildOfType(this, PbSyntaxStatement.class);
  }

  @Override
  @Nullable
  public PbPackageStatement getPackageStatement() {
    return CachedValuesManager.getCachedValue(
        this,
        () ->
            Result.create(
              PsiTreeUtil.getChildOfType(PbFileImpl.this, PbPackageStatement.class),
              PbCompositeModificationTracker.byElement(this)));
  }

  @Override
  @NotNull
  public List<PbImportStatement> getImportStatements() {
    return CachedValuesManager.getCachedValue(
        this,
        () ->
            Result.create(
                PsiTreeUtil.getChildrenOfTypeAsList(PbFileImpl.this, PbImportStatement.class),
                PbCompositeModificationTracker.byElement(this)));
  }

  @Override
  @NotNull
  public SyntaxLevel getSyntaxLevel() {
    PbSyntaxStatement statement = getSyntaxStatement();
    SyntaxLevel level = null;
    if (statement != null) {
      level = statement.getSyntaxLevel();
    }
    return level != null ? level : SyntaxLevel.PROTO2;
  }

  @Override
  @NotNull
  public QualifiedName getPackageQualifiedName() {
    PbPackageStatement packageStatement = getPackageStatement();
    if (packageStatement != null) {
      PbPackageName packageName = packageStatement.getPackageName();
      if (packageName != null) {
        QualifiedName packageQualifiedName = packageName.getQualifiedName();
        if (packageQualifiedName != null) {
          return packageQualifiedName;
        }
      }
    }
    // When no package name is specified, return an empty QualifiedName representing the default
    // package.
    return PbPsiUtil.EMPTY_QUALIFIED_NAME;
  }

  @Nullable
  @Override
  public QualifiedName getChildScope() {
    // The file itself doesn't declare a scope. Its package name elements, if defined, provide the
    // child scope for elements in the package.
    return PbPsiUtil.EMPTY_QUALIFIED_NAME;
  }

  @Override
  public PbFile getPbFile() {
    return this;
  }

  @NotNull
  @Override
  public List<PbStatement> getStatements() {
    return PbPsiImplUtil.getStatements(this);
  }

  @NotNull
  @Override
  public List<PbOptionStatement> getOptionStatements() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbOptionStatement.class);
  }

  @NotNull
  @Override
  public QualifiedName getDescriptorOptionsTypeName(Descriptor descriptor) {
    return DescriptorOptionType.FILE_OPTIONS.forDescriptor(descriptor);
  }

  @NotNull
  @Override
  public Map<String, Collection<PbSymbol>> getSymbolMap() {
    // The file's symbol map is equivalent to the symbol map for an empty package name. For example,
    // if the file's declared package is "com.foo.bar", its symbol map should contain only "com".
    return getPackageSymbolMap(PbPsiUtil.EMPTY_QUALIFIED_NAME);
  }

  @NotNull
  @Override
  public Map<String, Collection<PbSymbol>> getPackageSymbolMap(QualifiedName packageName) {
    QualifiedName declaredPackageName = getPackageQualifiedName();
    if (declaredPackageName.equals(packageName)) {
      // The file's full package name. Compute, cache, and return all of the non-package symbols
      // defined in the file.
      return PbPsiImplUtil.getCachedSymbolMap(this);
    } else if (declaredPackageName.matchesPrefix(packageName)) {
      // A substring of the declared package name was specified. It is the parent of only one
      // element: the next component of the declared package.
      PbPackageName packageElement = findPackageChildForName(packageName);
      if (packageElement == null) {
        // This shouldn't happen since the result of getPackageQualifiedName() matched.
        logger.error(
            String.format(
                "Failed to find a child package for name '%s' even though getPackageQualifiedName()"
                    + " returned '%s'",
                packageName, declaredPackageName));
        return ImmutableMap.of();
      }
      String packageElementName = packageElement.getName();
      if (packageElementName == null) {
        return ImmutableMap.of();
      }
      return ImmutableListMultimap.of(packageElementName, ((PbSymbol)packageElement)).asMap();
    } else {
      // Package name is not a prefix of this file's declared package.
      return ImmutableMap.of();
    }
  }

  @NotNull
  @Override
  public PbSymbolOwner getPrimarySymbolOwner() {
    PbPackageStatement packageStatement = getPackageStatement();
    if (packageStatement == null) {
      return this;
    }
    PbPackageName packageName = packageStatement.getPackageName();
    if (packageName == null) {
      return this;
    }
    return packageName;
  }

  @NotNull
  @Override
  public QualifiedName getExtensionOptionScope() {
    // Unlike other owner types, files don't chop off the last component of the container scope.
    return getPackageQualifiedName();
  }

  @NotNull
  @Override
  public Map<QualifiedName, Collection<PbSymbol>> getLocalQualifiedSymbolMap() {
    return CachedValuesManager.getCachedValue(
        this,
        () ->
            Result.create(
                computeLocalQualifiedSymbolMap(), PbCompositeModificationTracker.byElement(this)))
        .asMap();
  }

  @NotNull
  @Override
  public Map<QualifiedName, Collection<PbSymbol>> getExportedQualifiedSymbolMap() {
    return CachedValuesManager.getCachedValue(
        this,
        () ->
            Result.create(
                computeExportedQualifiedSymbolMap(), PbCompositeModificationTracker.byElement(this)))
        .asMap();
  }

  @NotNull
  @Override
  public Map<QualifiedName, Collection<PbSymbol>> getFullQualifiedSymbolMap() {
    return CachedValuesManager.getCachedValue(
        this,
        () ->
            Result.create(
                computeFullQualifiedSymbolMap(), PbCompositeModificationTracker.byElement(this)))
        .asMap();
  }

  private ImmutableMultimap<QualifiedName, PbSymbol> computeLocalQualifiedSymbolMap() {
    ImmutableMultimap.Builder<QualifiedName, PbSymbol> builder = ImmutableMultimap.builder();
    addSymbolsRecursively(this, builder);
    return builder.build();
  }

  private void addSymbolsRecursively(
      PbSymbolOwner parent, ImmutableMultimap.Builder<QualifiedName, PbSymbol> builder) {
    for (PbSymbol symbol : parent.getSymbols()) {
      QualifiedName symbolQualifiedName = symbol.getQualifiedName();
      if (symbolQualifiedName != null) {
        builder.put(symbolQualifiedName, symbol);
      }
      if (symbol instanceof PbSymbolOwner) {
        addSymbolsRecursively((PbSymbolOwner) symbol, builder);
      }
    }
  }

  private ImmutableMultimap<QualifiedName, PbSymbol> computeExportedQualifiedSymbolMap() {
    // Return all local symbols from this file and all files in the transitive set of public
    // imports.
    ImmutableMultimap.Builder<QualifiedName, PbSymbol> builder = ImmutableListMultimap.builder();

    getLocalQualifiedSymbolMap().forEach(builder::putAll);
    for (PbFile importedFile : getImportedFileList(/* includePrivate= */ false)) {
      importedFile.getLocalQualifiedSymbolMap().forEach(builder::putAll);
    }
    return builder.build();
  }

  private ImmutableSetMultimap<QualifiedName, PbSymbol> computeFullQualifiedSymbolMap() {
    // Return all local symbols from this file and exported symbols from all imported files.
    ImmutableSetMultimap.Builder<QualifiedName, PbSymbol> builder = ImmutableSetMultimap.builder();
    getLocalQualifiedSymbolMap().forEach(builder::putAll);
    for (PbFile importedFile : getImportedFileList(/* includePrivate= */ true)) {
      importedFile.getLocalQualifiedSymbolMap().forEach(builder::putAll);
    }
    return builder.build();
  }

  private PbPackageName findPackageChildForName(QualifiedName name) {
    PbPackageName currentPackage;
    PbPackageName lastChild = null;

    PbPackageStatement packageStatement = getPackageStatement();
    if (packageStatement == null) {
      return null;
    }

    // Iterate up the chain of declared package components until we find the component matching the
    // given name. Always remember the last child package to use as the result.
    currentPackage = packageStatement.getPackageName();
    while (currentPackage != null
        && currentPackage.getQualifiedName() != null
        && currentPackage.getQualifiedName().matchesPrefix(name)) {
      if (currentPackage.getQualifiedName().equals(name)) {
        // found it.
        return lastChild;
      }
      lastChild = currentPackage;
      currentPackage = currentPackage.getQualifier();
    }

    // If the given name was an empty, return the value of lastChild which should point to the
    // first component of the declared package. Else, return null because nothing was found.
    return name.getComponentCount() == 0 ? lastChild : null;
  }

  private List<PbFile> getImportedFileList(boolean includePrivate) {
    Set<PbFile> importedFiles = new LinkedHashSet<>();
    findImportsRecursively(this, importedFiles, includePrivate);
    return new ArrayList<>(importedFiles);
  }

  private static void findImportsRecursively(
      PbFile file, Collection<PbFile> imports, boolean includePrivate) {
    for (PbImportStatement pbImport : file.getImportStatements()) {
      if (!includePrivate && !pbImport.isPublic()) {
        continue;
      }
      PbImportName importName = pbImport.getImportName();
      if (importName == null) {
        continue;
      }
      PsiReference ref = importName.getReference();
      if (ref == null) {
        continue;
      }
      PsiElement possibleFile = ref.resolve();
      if (possibleFile instanceof PbFile importedFile) {
        if (imports.contains(importedFile)) {
          // TODO(volkman): do something with dependency cycle?
          continue;
        }
        imports.add(importedFile);
        // Recurse into this file, but only look at public imports.
        findImportsRecursively(importedFile, imports, /* includePrivate= */ false);
      }
    }
  }

  @Override
  public boolean processDeclarations(
      @NotNull PsiScopeProcessor processor,
      @NotNull ResolveState state,
      PsiElement lastParent,
      @NotNull PsiElement place) {
    for (PbStatement statement : getStatements()) {
      if (!processor.execute(statement, state)) {
        return false;
      }
    }
    return true;
  }
}
