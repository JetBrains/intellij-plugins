// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil.processUpPackageJsonFilesInAllScope;
import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static org.angular2.entities.Angular2EntitiesProvider.isDeclaredClass;

public class Angular2IvyUtil {

  public static boolean hasIvyMetadata(@NotNull PsiElement el) {
    return Optional.ofNullable(PsiUtilCore.getVirtualFile(el))
      .map(VirtualFile::getParent)
      .map(el.getManager()::findDirectory)
      .map(dir -> CachedValuesManager.getCachedValue(dir, () -> {
        VirtualFile f = dir.getVirtualFile();
        Ref<Boolean> result = new Ref<>(false);
        Ref<Integer> level = new Ref<>(0);
        processUpPackageJsonFilesInAllScope(f, packageJson -> {
          if (packageJson.getParent().findChild("__ivy_ngcc__") != null) { //NON-NLS
            result.set(true);
            return false;
          }
          level.set(level.get() + 1);
          //we need to check only 2 package.jsons
          return level.get() < 2;
        });
        return create(result.get(), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS);
      }))
      .orElse(false);
  }

  public static Angular2IvyEntity<?> getIvyEntity(@NotNull PsiElement element) {
    return getIvyEntity(element, false);
  }

  public static Angular2IvyEntity<?> getIvyEntity(@NotNull PsiElement element, boolean allowAbstractClasses) {
    final Angular2IvySymbolDef.Entity entityDef;
    if (element instanceof TypeScriptClass) {
      if (!isDeclaredClass((TypeScriptClass)element)) {
        return null;
      }
      entityDef = Angular2IvySymbolDef.get((TypeScriptClass)element, allowAbstractClasses);
    }
    else if (element instanceof TypeScriptField) {
      entityDef = Angular2IvySymbolDef.get((TypeScriptField)element, allowAbstractClasses);
    }
    else {
      entityDef = null;
    }

    if (entityDef == null) {
      return null;
    }

    return CachedValuesManager.getCachedValue(entityDef.getField(), () -> {
      return create(entityDef.createEntity(), ObjectUtils.notNull(entityDef.getContextClass(), entityDef.getField()));
    });
  }
}
