// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.lang.ecmascript6.psi.ES6ExportSpecifierAlias;
import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifier;
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier;
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil;
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import one.util.streamex.StreamEx;
import org.angular2.entities.metadata.stubs.Angular2MetadataModuleExportStub;
import org.angular2.entities.metadata.stubs.Angular2MetadataNodeModuleStub;
import org.angular2.lang.metadata.psi.MetadataElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.lang.javascript.ui.NodeModuleNamesUtil.PACKAGE_JSON;
import static com.intellij.openapi.util.Pair.create;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.entities.metadata.Angular2MetadataFileType.D_TS_SUFFIX;
import static org.angular2.entities.metadata.Angular2MetadataFileType.METADATA_SUFFIX;

public class Angular2MetadataNodeModule extends Angular2MetadataElement<Angular2MetadataNodeModuleStub> {

  public Angular2MetadataNodeModule(@NotNull Angular2MetadataNodeModuleStub element) {
    super(element);
  }

  public @NotNull <T extends PsiElement> Pair<PsiFile, T> locateFileAndMember(String memberName, Class<T> memberClass) {
    PsiFile definitionPsi = loadRelativeFile(StringUtil.trimEnd(getContainingFile().getName(), METADATA_SUFFIX), D_TS_SUFFIX);
    T result = null;
    if (definitionPsi instanceof JSFile) {
      ResolveResultSink sink = new ResolveResultSink(definitionPsi, memberName, true);
      ES6PsiUtil.processExportDeclarationInScope((JSFile)definitionPsi, new TypeScriptQualifiedItemProcessor<>(sink, definitionPsi));

      List<PsiElement> results = sink.getResults();
      if (results != null) {
        for (PsiElement res : results) {
          if (res instanceof ES6ExportSpecifierAlias) {
            res = ((ES6ExportSpecifierAlias)res).findAliasedElement();
          }
          else if (res instanceof ES6ImportExportSpecifier) {
            res = ((ES6ImportExportSpecifier)res).resolve();
          }
          result = tryCast(res, memberClass);
          if (result != null) {
            break;
          }
        }
      }
    }
    return create(definitionPsi, result);
  }

  @Override
  public MetadataElement findMember(@Nullable String name) {
    MetadataElement result = super.findMember(name);
    if (result != null) {
      return result;
    }
    return StreamEx.of(getStub().getChildrenStubs())
      .select(Angular2MetadataModuleExportStub.class)
      .map(StubBase::getPsi)
      .map(m -> m.findExport(name))
      .nonNull()
      .findFirst()
      .orElse(null);
  }

  @Override
  public String getName() {
    return getStub().getImportAs();
  }

  @Override
  public String toString() {
    return (getStub().getImportAs() != null ? getStub().getImportAs() + " " : "")
           + "<metadata node module>";
  }

  public boolean isPackageTypingsRoot() {
    return CachedValuesManager.getCachedValue(this, () ->
      CachedValueProvider.Result.create(
        checkPackageJson(PACKAGE_JSON, "./")
        || checkPackageJson(StringUtil.trimEnd(getContainingFile().getName(), METADATA_SUFFIX) + "/" + PACKAGE_JSON,
                            "../"),
        VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS));
  }

  private boolean checkPackageJson(@NotNull String path, @NotNull String prefix) {
    VirtualFile sourceFile = getContainingFile().getViewProvider().getVirtualFile();
    VirtualFile packageFile = doIfNotNull(sourceFile.getParent(),
                                          parentDir -> parentDir.findFileByRelativePath(path));
    if (packageFile == null) {
      return false;
    }
    String mainFile = PackageJsonData.getOrCreate(packageFile).getDefaultMain();
    if (mainFile == null) {
      return false;
    }
    return (StringUtil.trimEnd(getContainingFile().getName(), METADATA_SUFFIX) + D_TS_SUFFIX)
      .equals(StringUtil.trimStart(mainFile, prefix));
  }
}
