// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiUtilCore
import org.angular2.entities.Angular2EntitiesProvider.isDeclaredClass

object Angular2IvyUtil {
  fun hasIvyMetadata(el: PsiElement): Boolean =
    PsiUtilCore.getVirtualFile(el)
      ?.parent
      ?.let { el.manager.findDirectory(it) }
      ?.let { dir ->
        CachedValuesManager.getCachedValue(dir) {
          var result = false
          val packageJson = PackageJsonUtil.findUpPackageJson(dir.virtualFile)
          if (packageJson != null && PackageJsonUtil.isPackageJsonWithTopLevelProperty(packageJson, "__processed_by_ivy_ngcc__")) {
            result = true
          }
          create(result, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
        }
      }
    ?: false

  @JvmOverloads
  fun getIvyEntity(element: PsiElement, allowAbstractClasses: Boolean = false): Angular2IvyEntity<*>? {
    val entityDef: Angular2IvySymbolDef.Entity?
    when (element) {
      is TypeScriptClass -> {
        if (!isDeclaredClass(element)) {
          return null
        }
        entityDef = Angular2IvySymbolDef.get(element, allowAbstractClasses)
      }
      is TypeScriptField -> {
        entityDef = Angular2IvySymbolDef.get(element, allowAbstractClasses)
      }
      else -> {
        entityDef = null
      }
    }

    return if (entityDef == null) {
      null
    }
    else CachedValuesManager.getCachedValue(entityDef.field) {
      create(entityDef.createEntity(), entityDef.contextClass ?: entityDef.field)
    }
  }
}
