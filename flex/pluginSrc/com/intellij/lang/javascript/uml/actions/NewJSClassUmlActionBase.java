package com.intellij.lang.javascript.uml.actions;

import com.intellij.diagram.DiagramBuilder;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.actions.DiagramCreateNewElementAction;
import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.uml.FlashUmlDataModel;
import com.intellij.lang.javascript.validation.fixes.CreateClassParameters;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public abstract class NewJSClassUmlActionBase extends DiagramCreateNewElementAction<Object, CreateClassParameters> {

  protected NewJSClassUmlActionBase(String name, String description, Icon icon) {
    super(name, description, icon);
  }

  static JSClass getSuperClass(final CreateClassParameters params) {
    final JSClass superClass;
    if (params.getSuperclassFqn() != null) {
      Module module = ModuleUtilCore.findModuleForPsiElement(params.getTargetDirectory());
      GlobalSearchScope superClassScope = module != null
                                          ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
                                          : GlobalSearchScope.projectScope(params.getTargetDirectory().getProject());
      PsiElement byQName = JSResolveUtil.findClassByQName(params.getSuperclassFqn(), superClassScope);
      superClass = byQName instanceof JSClass ? (JSClass)byQName : null;
    }
    else {
      superClass = null;
    }
    return superClass;
  }

  @Override
  public boolean isEnabled(AnActionEvent e, DiagramBuilder b) {
    return b != null && b.getDataModel() instanceof FlashUmlDataModel;
  }

  @Override
  public CreateClassParameters prepare(AnActionEvent e) {
    DiagramBuilder diagramBuilder = getBuilder(e);
    if (diagramBuilder == null) return null;

    Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    return showDialog(project, getPackageToCreateIn((FlashUmlDataModel)diagramBuilder.getDataModel()));
  }

  @Nullable
  protected abstract CreateClassParameters showDialog(Project project, Pair<PsiDirectory, String> dirAndPackage);

  @Nullable
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
