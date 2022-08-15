// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.ui.newclass;

import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.validation.fixes.CreateClassParameters;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WizardModel implements CreateClassParameters {

  private final PsiElement myContext;
  private final Module myModule;
  private final boolean myCreatingDependencyNotDependent;

  private String myClassName;
  private String myPackageName;
  private String myTemplateName;
  private PsiDirectory myTargetDirectory;
  private String mySuperclassFqn;
  private Collection<String> myInterfacesFqns;
  private final Map<String, Map<String, String>> myCustomTemplateAttributes = new HashMap<>();

  public WizardModel(@NotNull final PsiElement context, final boolean creatingDependencyNotDependent) {
    myContext = context;
    myCreatingDependencyNotDependent = creatingDependencyNotDependent;
    myModule = ModuleUtilCore.findModuleForPsiElement(context);
  }

  @Override
  public String getClassName() {
    return myClassName;
  }

  public void setClassName(final String className) {
    myClassName = className;
  }

  @Override
  public String getPackageName() {
    return myPackageName;
  }

  public void setPackageName(final String packageName) {
    myPackageName = packageName;
  }

  @Override
  public String getTemplateName() {
    return myTemplateName;
  }

  public void setTemplateName(final String templateName) {
    myTemplateName = templateName;
  }

  @Override
  public PsiDirectory getTargetDirectory() {
    return myTargetDirectory;
  }

  @Nullable
  @Override
  public String getSuperclassFqn() {
    return mySuperclassFqn;
  }

  public void setSuperclassFqn(final String superclassFqn) {
    mySuperclassFqn = superclassFqn;
  }

  @Override
  public Collection<String> getInterfacesFqns() {
    return myInterfacesFqns;
  }

  public void setInterfacesFqns(final Collection<String> interfacesFqns) {
    myInterfacesFqns = interfacesFqns;
  }

  public void setCustomVariables(final String templateName, final Collection<String> customVariablesNames) {
    Map<String, String> map = myCustomTemplateAttributes.get(templateName);
    if (map == null) {
      map = new HashMap<>();
      myCustomTemplateAttributes.put(templateName, map);
    }
    map.keySet().retainAll(customVariablesNames);
    for (String name : customVariablesNames) {
      if (!map.containsKey(name)) {
        map.put(name, null);
      }
    }
  }

  public Map<String, String> getCustomTemplateAttributes(String templateName) {
    return myCustomTemplateAttributes.get(templateName);
  }

  public void setCustomTemplateAttributes(final String templateName, final Map<String, String> map) {
    myCustomTemplateAttributes.put(templateName, map);
  }

  @Override
  public Map<String, String> getTemplateAttributes() {
    return myCustomTemplateAttributes.get(myTemplateName);
  }

  public boolean commit() {
    Pair<GlobalSearchScope, PsiDirectory> scopeAndBaseDir = getTargetClassScopeAndBaseDir();
    if (myContext instanceof PsiDirectory &&
        getPackageName()
          .equals(DirectoryIndex.getInstance(myContext.getProject()).getPackageName(((PsiDirectory)myContext).getVirtualFile()))) {
      // user has not changed package
      myTargetDirectory = (PsiDirectory)myContext;
      return true;
    }
    else {
      myTargetDirectory =
        JSRefactoringUtil.chooseOrCreateDirectoryForClass(myContext.getProject(), myModule, scopeAndBaseDir.first, getPackageName(),
                                                          getClassName(), scopeAndBaseDir.second, ThreeState.UNSURE);
      return myTargetDirectory != null;
    }
  }

  public Pair<GlobalSearchScope, PsiDirectory> getTargetClassScopeAndBaseDir() {
    GlobalSearchScope scope;
    if (myModule != null) {
      PsiDirectory baseDir = myContext instanceof PsiDirectory ? (PsiDirectory)myContext : PlatformPackageUtil.getDirectory(myContext);
      if (myCreatingDependencyNotDependent) {
        scope = PlatformPackageUtil.adjustScope(baseDir, GlobalSearchScope.moduleWithDependenciesScope(myModule), false, true);
      }
      else {
        scope = PlatformPackageUtil.adjustScope(baseDir, GlobalSearchScope.moduleWithDependentsScope(myModule), true, false);
      }
      return Pair.create(scope, baseDir);
    }
    else {
      return Pair.create(GlobalSearchScope.projectScope(myContext.getProject()), null);
    }
  }
}
