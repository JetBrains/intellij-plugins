package com.intellij.lang.javascript.uml.actions;

import com.intellij.diagram.DiagramBuilder;
import com.intellij.diagram.DiagramDataModel;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.actions.DiagramCreateNewElementAction;
import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.uml.FlashUmlDataModel;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public abstract class NewJSClassUmlActionBase extends DiagramCreateNewElementAction<Object, NewJSClassUmlActionBase.PreparationData> {

  public static class PreparationData {
    public final String classQName;
    public final String templateName;
    public final Map<String, String> customProperties;
    public final PsiDirectory targetDirectory;

    public PreparationData(String classQName, String templateName, Map<String, String> customProperties, PsiDirectory targetDirectory) {
      this.classQName = classQName;
      this.templateName = templateName;
      this.customProperties = customProperties;
      this.targetDirectory = targetDirectory;
    }
  }

  protected NewJSClassUmlActionBase(String name, String description, Icon icon) {
    super(name, description, icon);
  }

  @Override
  public boolean isEnabled(AnActionEvent e, DiagramBuilder b) {
    return b != null && b.getDataModel() instanceof FlashUmlDataModel;
  }

  @Override
  public PreparationData prepare(AnActionEvent e) {
    DiagramBuilder diagramBuilder = getBuilder(e);
    if (diagramBuilder == null) return null;

    Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    return showDialog(project, getPackageToCreateIn((FlashUmlDataModel)diagramBuilder.getDataModel()));
  }

  @Nullable
  protected abstract PreparationData showDialog(Project project, Pair<PsiDirectory, String> dirAndPackage);

  @Nullable
  protected abstract JSClass getClass(PsiFile file, PreparationData data);

  @Override
  public Object createElement(DiagramDataModel<Object> objectUmlDataModel, PreparationData data, AnActionEvent event) {
    try {
      PsiFile file = (PsiFile)CreateClassOrInterfaceAction.createClass(StringUtil.getShortName(data.classQName),
                                                                       StringUtil.getPackageName(data.classQName),
                                                                       data.targetDirectory, data.templateName);
      return getClass(file, data);
    }
    catch (Exception e) {
      throw new IncorrectOperationException(e.getMessage(), e);
    }
  }

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
