// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.util.Pair
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.webSymbols.WebSymbolOrigin
import icons.AngularJSIcons
import org.angular2.Angular2Framework
import javax.swing.Icon

class Angular2SymbolOrigin(private val mySymbol: Angular2Symbol) : WebSymbolOrigin {

  private val versionAndName: Pair<String, String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val source = if (mySymbol is PsiSourcedWebSymbol)
      (mySymbol as PsiSourcedWebSymbol).source
    else
      null
    val psiFile = source?.containingFile
    val virtualFile = psiFile?.virtualFile
    val pkgJson = if (virtualFile != null) PackageJsonUtil.findUpPackageJson(virtualFile) else null
    val data = if (pkgJson != null) PackageJsonData.getOrCreate(pkgJson) else null
    if (data != null)
      Pair.create(data.name, data.version?.toString())
    else
      Pair.create(null, null)
  }

  override val framework: String
    get() = Angular2Framework.ID

  override val library: String?
    get() = versionAndName.first

  override val version: String?
    get() = versionAndName.second

  override val defaultIcon: Icon
    get() = AngularJSIcons.Angular2
}
