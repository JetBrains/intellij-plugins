// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.webSymbols.PsiSourcedWebSymbol;
import com.intellij.webSymbols.WebSymbolsContainer;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.util.text.SemVer;
import icons.AngularJSIcons;
import org.angular2.Angular2Framework;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public class Angular2SymbolOrigin implements WebSymbolsContainer.Origin {

  private final Angular2Symbol mySymbol;

  private Pair<String, String> nameAndVersion;

  public Angular2SymbolOrigin(Angular2Symbol symbol) { mySymbol = symbol; }

  private @NotNull Pair<String, String> getVersionAndName() {
    if (nameAndVersion == null) {
      var source = mySymbol instanceof PsiSourcedWebSymbol
                   ? ((PsiSourcedWebSymbol)mySymbol).getSource() : null;
      var psiFile = source != null ? source.getContainingFile() : null;
      var virtualFile = psiFile != null ? psiFile.getVirtualFile() : null;
      var pkgJson = virtualFile != null ? PackageJsonUtil.findUpPackageJson(virtualFile) : null;
      var data = pkgJson != null ? PackageJsonData.getOrCreate(pkgJson) : null;
      nameAndVersion = data != null
                       ? Pair.create(data.getName(), doIfNotNull(data.getVersion(), SemVer::toString))
                       : Pair.create(null, null);
    }
    return nameAndVersion;
  }

  @Nullable
  @Override
  public String getFramework() {
    return Angular2Framework.ID;
  }

  @Nullable
  @Override
  public String getPackageName() {
    return getVersionAndName().first;
  }

  @Nullable
  @Override
  public String getVersion() {
    return getVersionAndName().second;
  }

  @Nullable
  @Override
  public Icon getDefaultIcon() {
    return AngularJSIcons.Angular2;
  }
}
