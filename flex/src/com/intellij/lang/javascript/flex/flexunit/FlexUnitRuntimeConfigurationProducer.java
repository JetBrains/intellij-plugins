// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FlexUnitRuntimeConfigurationProducer extends LazyRunConfigurationProducer<FlexUnitRunConfiguration> {
  @NotNull
  @Override
  public ConfigurationFactory getConfigurationFactory() {
    return ConfigurationTypeUtil.findConfigurationType(FlexUnitRunConfigurationType.class);
  }

  @Override
  protected boolean setupConfigurationFromContext(@NotNull final FlexUnitRunConfiguration configuration,
                                                  @NotNull final ConfigurationContext context,
                                                  @NotNull final Ref<PsiElement> sourceElement) {
    final Module module = context.getModule();
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) return false;

    final PsiElement element = findTestElement(context.getPsiLocation());
    if (element == null) return false;

    if (!configureRunnerParameters(configuration.getRunnerParameters(), module, element)) return false;

    configuration.setGeneratedName();
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(@NotNull final FlexUnitRunConfiguration configuration,
                                            @NotNull final ConfigurationContext context) {
    final Module module = context.getModule();
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) return false;

    final PsiElement element = findTestElement(context.getPsiLocation());
    if (element == null) return false;

    final FlexUnitRunnerParameters fakeParams = new FlexUnitRunnerParameters();
    if (!configureRunnerParameters(fakeParams, module, element)) return false;

    final FlexUnitRunnerParameters params = configuration.getRunnerParameters();
    if (params.getModuleName().equals(fakeParams.getModuleName())
        && params.getScope() == fakeParams.getScope()
        && (params.getScope() != FlexUnitRunnerParameters.Scope.Package || params.getPackageName().equals(fakeParams.getPackageName()))
        && (params.getScope() == FlexUnitRunnerParameters.Scope.Package || params.getClassName().equals(fakeParams.getClassName()))
        && (params.getScope() != FlexUnitRunnerParameters.Scope.Method || params.getMethodName().equals(fakeParams.getMethodName()))) {
      return true;
    }

    return false;
  }

  private static boolean configureRunnerParameters(final FlexUnitRunnerParameters params, final Module module, final PsiElement element) {
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);
    FlexUnitSupport support = null;
    FlexBuildConfiguration bc = null;

    if (module.getName().equals(params.getModuleName()) && !params.getBCName().isEmpty()) {
      bc = manager.findConfigurationByName(params.getBCName());
      if (bc != null) {
        support = FlexUnitSupport.getSupport(bc, module);
      }
    }

    if (support == null) {
      bc = manager.getActiveConfiguration();
      support = FlexUnitSupport.getSupport(bc, module);
    }

    if (support == null) {
      for (FlexBuildConfiguration anyBC : manager.getBuildConfigurations()) {
        bc = anyBC;
        support = FlexUnitSupport.getSupport(bc, module);
        if (support != null) {
          break;
        }
      }
    }

    if (support == null) {
      return false;
    }

    params.setModuleName(module.getName());
    params.setBCName(bc.getName());

    if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      if (bc.getAndroidPackagingOptions().isEnabled()) {
        params.setAppDescriptorForEmulator(FlashRunnerParameters.AppDescriptorForEmulator.Android);
      }
      else if (bc.getIosPackagingOptions().isEnabled()) {
        params.setAppDescriptorForEmulator(FlashRunnerParameters.AppDescriptorForEmulator.IOS);
      }
    }

    if (element instanceof JSClass) {
      final JSClass clazz = (JSClass)element;
      if (!support.isTestClass(clazz, true)) return false;

      forClass(clazz, params);
    }
    else if (element instanceof JSFunction method) {
      final String methodName = method.getName();
      final JSClass clazz = (JSClass)element.getParent();

      if (!support.isTestClass(clazz, true)) return false;

      if (methodName == null || !support.isTestMethod(method)) {
        forClass(clazz, params);
      }
      else {
        params.setClassName(clazz.getQualifiedName());
        params.setMethodName(methodName);
        params.setScope(FlexUnitRunnerParameters.Scope.Method);
      }
    }
    else if (element instanceof PsiDirectory) {
      if (!forDirectory((PsiDirectory)element, module, params)) return false;
    }
    else if (element instanceof PsiDirectoryContainer) {
      if (!forPackage((PsiDirectoryContainer)element, module, params)) return false;
    }
    else {
      return false;
    }

    return true;
  }

  private static boolean forPackage(PsiDirectoryContainer psiPackage, Module module, FlexUnitRunnerParameters params) {
    if (module == null) return false;
    for (PsiDirectory directory : psiPackage.getDirectories(module.getModuleScope())) {
      if (forDirectory(directory, module, params)) {
        return true;
      }
    }
    return false;
  }

  private static boolean forDirectory(PsiDirectory directory, Module module, FlexUnitRunnerParameters params) {
    final VirtualFile file = directory.getVirtualFile();
    ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(directory.getProject()).getFileIndex();
    VirtualFile rootForFile = projectFileIndex.getSourceRootForFile(file);
    if (rootForFile == null) return false;

    String packageName = VfsUtilCore.getRelativePath(file, rootForFile, '.');
    if (packageName == null || !FlexUtils.packageExists(packageName, GlobalSearchScope.moduleScope(module))) return false;

    params.setPackageName(packageName);
    params.setScope(FlexUnitRunnerParameters.Scope.Package);
    return true;
  }

  private static void forClass(JSClass clazz, FlexUnitRunnerParameters params) {
    params.setClassName(clazz.getQualifiedName());
    params.setScope(FlexUnitRunnerParameters.Scope.Class);
  }

  @Nullable
  private static PsiElement findTestElement(PsiElement element) {
    if (element.getLanguage().isKindOf(JavaScriptSupportLoader.JAVASCRIPT.getLanguage())) {
      final PsiNamedElement parent = PsiTreeUtil.getNonStrictParentOfType(element, JSFunction.class, JSClass.class, JSFile.class);
      if (parent instanceof JSClass) {
        return parent;
      }
      if (parent instanceof JSFunction && parent.getParent() instanceof JSClass) {
        return parent;
      }
      if (parent instanceof JSFile) {
        return JSPsiImplUtils.findClass((JSFile)parent);
      }
    }
    else if (element instanceof PsiDirectory) {
      return element;
    }
    else if (element instanceof PsiDirectoryContainer) {
      return element;
    }

    return null;
  }
}
