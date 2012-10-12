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
public class FlashUmlProvider extends DiagramProvider<Object> {
  public static final String ID = "Flash";

  private final FlashUmlVisibilityManager myVisibilityManager = new FlashUmlVisibilityManager();
  private final FlashUmlNodeContentManager myNodeContentManager = new FlashUmlNodeContentManager();
  private final FlashUmlElementManager myElementManager = new FlashUmlElementManager(this);
  private final FlashUmlVfsResolver myVfsResolver = new FlashUmlVfsResolver();
  private final FlashUmlRelationshipManager myRelationshipManager = new FlashUmlRelationshipManager();
  private final FlashUmlColorManager myColorManager = new FlashUmlColorManager();
  private final FlashUmlExtras myExtras = new FlashUmlExtras();

  @Pattern("[a-zA-Z0-9_-]*")
  @Override
  public String getID() {
    return ID;
  }

  public FlashUmlVisibilityManager createVisibilityManager() {
    return myVisibilityManager;
  }

  public FlashUmlNodeContentManager getNodeContentManager() {
    return myNodeContentManager;
  }

  public FlashUmlElementManager getElementManager() {
    return myElementManager;
  }

  public FlashUmlVfsResolver getVfsResolver() {
    return myVfsResolver;
  }

  public FlashUmlRelationshipManager getRelationshipManager() {
    return myRelationshipManager;
  }

  public FlashUmlDataModel createDataModel(@NotNull Project project,
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
    return new FlashUmlDataModel(project, element, file, this);
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
  public FlashUmlExtras getExtras() {
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
    return new FlashUmlEdgeCreationPolicy();
  }
}
