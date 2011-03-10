package com.intellij.lang.javascript.uml;

import com.intellij.diagram.*;
import com.intellij.diagram.actions.DiagramCreateNewNodeElementAction;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.uml.actions.*;
import com.intellij.lang.javascript.uml.actions.NewFlexComponentUmlAction;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.diagram.actions.DiagramCreateNewElementAction;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Kirill Safonov
 * @author Konstantin Bulenkov
 */
public class JSUmlProvider extends DiagramProvider<Object> {
  public static final String ID = "JS";

  private final JSUmlVisibilityManager myVisibilityManager = new JSUmlVisibilityManager();
  private final JSNodeContentManager myNodeContentManager = new JSNodeContentManager();
  private final JSElementManager myElementManager = new JSElementManager(this);
  private final JSVfsResolver myVfsResolver = new JSVfsResolver();
  private final JSUmlRelationshipManager myRelationshipManager = new JSUmlRelationshipManager();
  private final JSUmlColorManager myColorManager = new JSUmlColorManager();
  private final JSUmlExtras myExtras = new JSUmlExtras();

  @Pattern("[a-zA-Z0-9_-]*")
  @Override
  public String getID() {
    return ID;
  }

  public JSUmlVisibilityManager createVisibilityManager() {
    return myVisibilityManager;
  }

  public JSNodeContentManager getNodeContentManager() {
    return myNodeContentManager;
  }

  public JSElementManager getElementManager() {
    return myElementManager;
  }

  public JSVfsResolver getVfsResolver() {
    return myVfsResolver;
  }

  public JSUmlRelationshipManager getRelationshipManager() {
    return myRelationshipManager;
  }

  public JSUmlDataModel createDataModel(@NotNull Project project,
                                        @Nullable Object element,
                                        @Nullable VirtualFile file,
                                        DiagramPresentationModel presentationModel) {
    if (element instanceof JSFile) {
      element = JSPsiImplUtils.findQualifiedElement((JSFile)element);
    }
    else if (element instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)element)) {
      element = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)element);
    }
    else if (element instanceof PsiDirectory) {
      PsiDirectory directory = (PsiDirectory)element;
      element = JSResolveUtil.getExpectedPackageNameFromFile(directory.getVirtualFile(), directory.getProject());
    }
    return new JSUmlDataModel(project, element, file, this);
  }

  @Override
  public DiagramScopeManager<Object> createScopeManager(Project project) {
    return new DiagramPsiScopeManager<Object>(project);
  }

  @Override
  public DiagramColorManager getColorManager() {
    return myColorManager;
  }

  @NotNull
  @Override
  public JSUmlExtras getExtras() {
    return myExtras;
  }

  public static final DiagramCreateNewElementAction[] ELEMENT_ACTIONS = {
    new NewActionScriptClassUmlAction(),
    new NewFlexComponentUmlAction()
  };

  public static final DiagramCreateNewNodeElementAction[] NODE_ACTIONS = {
    new JSCreateFieldAction(),
    new JSCreateMethodAction(),
    new JSCreateConstructorAction()
  };

  @Override
  public DiagramCreateNewElementAction<Object, ?>[] getCreateNewActions() {
    //noinspection unchecked
    return ELEMENT_ACTIONS;
  }

  @Override
  public DiagramCreateNewNodeElementAction<Object, ?>[] getCreateNewNodeElementActions() {
    //noinspection unchecked
    return NODE_ACTIONS;
  }

  @Override
  public String getPresentableName() {
    return JSBundle.message("js.uml.presentable.name");
  }

  @Override
  public DiagramEdgeCreationPolicy<Object> getEdgeCreationPolicy() {
    return new JSUmlEdgeCreationPolicy();
  }
}
