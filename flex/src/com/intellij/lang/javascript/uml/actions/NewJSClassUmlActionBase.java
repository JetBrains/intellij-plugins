// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.uml.actions;

import com.intellij.diagram.DiagramBuilder;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.actions.DiagramCreateNewElementAction;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.uml.FlashUmlDataModel;
import com.intellij.lang.javascript.validation.fixes.CreateClassParameters;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public abstract class NewJSClassUmlActionBase extends DiagramCreateNewElementAction<Object, CreateClassParameters> {
  protected NewJSClassUmlActionBase(@NotNull Supplier<String> name, @NotNull Supplier<String> description, Icon icon) {
    super(name, description, icon);
  }

  static JSClass getSuperClass(final CreateClassParameters params) {
    final JSClass superClass;
    if (params.getSuperclassFqn() != null) {
      Module module = ModuleUtilCore.findModuleForPsiElement(params.getTargetDirectory());
      GlobalSearchScope superClassScope = module != null
                                          ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
                                          : GlobalSearchScope.projectScope(params.getTargetDirectory().getProject());
      PsiElement byQName = ActionScriptClassResolver.findClassByQNameStatic(params.getSuperclassFqn(), superClassScope);
      superClass = byQName instanceof JSClass ? (JSClass)byQName : null;
    }
    else {
      superClass = null;
    }
    return superClass;
  }

  @Override
  public boolean isEnabled(@NotNull AnActionEvent e, DiagramBuilder b) {
    return b != null && b.getDataModel() instanceof FlashUmlDataModel;
  }

  @Override
  @Nullable
  public CreateClassParameters prepare(@NotNull AnActionEvent e) {
    DiagramBuilder diagramBuilder = getBuilder(e);
    if (diagramBuilder == null) return null;

    final Project project = e.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      return null;
    }

    Pair<PsiDirectory, String> dirAndPackage = getPackageToCreateIn((FlashUmlDataModel)diagramBuilder.getDataModel());
    if (dirAndPackage.first == null) {
      Collection<VirtualFile> dirs =
        DirectoryIndex.getInstance(project).getDirectoriesByPackageName(dirAndPackage.second, false).findAll();
      final PsiManager psiManager = PsiManager.getInstance(project);
      PsiDirectory[] psiDirs = ContainerUtil.map2Array(dirs, PsiDirectory.class, virtualFile -> psiManager.findDirectory(virtualFile));
      PsiDirectory dir = DirectoryChooserUtil.selectDirectory(project, psiDirs, null, null);
      if (dir == null) {
        return null;
      }
      dirAndPackage = Pair.create(dir, dirAndPackage.second);
    }
    return showDialog(project, dirAndPackage);
  }

  @Nullable
  protected abstract CreateClassParameters showDialog(Project project, Pair<PsiDirectory, String> dirAndPackage);

  private static Pair<PsiDirectory, String> getPackageToCreateIn(FlashUmlDataModel model) {
    final DiagramBuilder builder = model.getBuilder();

    String aPackage = null;
    PsiDirectory directory = null;
    final List<Node> nodes = GraphViewUtil.getSelectedNodes(builder.getGraph());
    if (nodes.size() == 1) {
      DiagramNode node = builder.getNodeObject(nodes.get(0));
      if (node != null) {
        if (node.getIdentifyingElement() instanceof String) {
          aPackage = (String)node.getIdentifyingElement();
          directory = null;
        }
        else {
          final JSClass selectedClass = (JSClass)node.getIdentifyingElement();
          directory = PlatformPackageUtil.getDirectory(selectedClass);
          aPackage = StringUtil.getPackageName(selectedClass.getQualifiedName());
        }
      }
    }

    if (aPackage == null) {
      JSClass initialClass = (JSClass)model.getInitialElement();
      if (initialClass != null) {
        directory = PlatformPackageUtil.getDirectory(initialClass);
        aPackage = StringUtil.getPackageName(initialClass.getQualifiedName());
      }
      else {
        directory = null;
        aPackage = model.getInitialPackage();
      }
    }

    return Pair.create(directory, aPackage);
  }
}
