// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.mxml;

import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.PredefinedImportSet;
import com.intellij.lang.javascript.flex.ScopedImportSet;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 */
public final class MxmlImplicitImports {
  // flex2.compiler.mxml.lang.StandardDefs
  private static @NonNls final ScopedImportSet
    standardMxmlImports = new PredefinedImportSet("mx.styles.*", "mx.binding.*", "mx.core.mx_internal",
    "mx.core.IDeferredInstance", "mx.core.IFactory", "mx.core.IPropertyChangeNotifier", "mx.core.ClassFactory",
    "mx.core.DeferredInstanceFromClass", "mx.core.DeferredInstanceFromFunction");

  private static @NonNls final ScopedImportSet airOnlyImplicitImports =
    new PredefinedImportSet("flash.data.*", "flash.desktop.*", "flash.filesystem.*", "flash.html.*", "flash.html.script.*");

  // common for Flex 3 and Flex 4
  private static @NonNls final String[] commonImplicitImports =
    new String[]{"flash.accessibility.*", "flash.debugger.*", "flash.display.*", "flash.errors.*", "flash.events.*", "flash.external.*",
      "flash.geom.*", "flash.media.*", "flash.net.*", "flash.printing.*", "flash.profiler.*", "flash.system.*", "flash.text.*",
      "flash.ui.*", "flash.utils.*", "flash.xml.*"};

  private static @NonNls final ScopedImportSet flex3ImplicitImports =
    new PredefinedImportSet(ArrayUtil.append(commonImplicitImports, "flash.filters.*"));

  private static @NonNls final ScopedImportSet flex4ImplicitImports = new PredefinedImportSet(
    ArrayUtil.mergeArrays(commonImplicitImports, "mx.filters.*", "mx.core.IFlexModuleFactory"));

  public static boolean resolveTypeNameUsingImplicitImports(final ResolveProcessor resolveProcessor, @NotNull PsiNamedElement scope) {
    final PsiElement context = scope.getContext();

    if (context != null) {
      XmlTag tag = PsiTreeUtil.getParentOfType(context, XmlTag.class, false);
      boolean flex4ns = tag != null && ArrayUtil.contains(JavaScriptSupportLoader.MXML_URI3, tag.knownNamespaces());

      ScopedImportSet flexImplicitImports = flex4ns ? flex4ImplicitImports : flex3ImplicitImports;
      if (!flexImplicitImports.tryResolveImportedClass(scope, resolveProcessor)) return false;

      if (!standardMxmlImports.tryResolveImportedClass(scope, resolveProcessor)) return false;

      if (isAirImportsNeeded(scope)) {
        if (!airOnlyImplicitImports.tryResolveImportedClass(scope, resolveProcessor)) return false;
      }
    }
    return true;
  }

  private static boolean isAirImportsNeeded(PsiElement element) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (module != null) {
      final FlexBuildConfiguration bc = ModuleType.get(module) instanceof FlexModuleType
                                        ? FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration()
                                        : null;
      return bc != null && (bc.getTargetPlatform() == TargetPlatform.Desktop || bc.getTargetPlatform() == TargetPlatform.Mobile);
    }

    return false;
  }
}
