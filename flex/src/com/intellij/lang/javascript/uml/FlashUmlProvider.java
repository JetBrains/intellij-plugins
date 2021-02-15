// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.uml;

import com.intellij.diagram.*;
import com.intellij.diagram.actions.DiagramCreateNewElementAction;
import com.intellij.diagram.actions.DiagramCreateNewNodeElementAction;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.uml.actions.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Kirill Safonov
 * @author Konstantin Bulenkov
 */
public final class FlashUmlProvider extends DiagramProvider<Object> {
  public static final String ID = "Flash";

  private final FlashUmlVisibilityManager myVisibilityManager = new FlashUmlVisibilityManager();
  private final FlashUmlNodeContentManager myNodeContentManager = new FlashUmlNodeContentManager();
  private final FlashUmlElementManager myElementManager = new FlashUmlElementManager();
  private final FlashUmlVfsResolver myVfsResolver = new FlashUmlVfsResolver();
  private final FlashUmlRelationshipManager myRelationshipManager = new FlashUmlRelationshipManager();
  private final FlashUmlColorManager myColorManager = new FlashUmlColorManager();
  private final FlashUmlExtras myExtras = new FlashUmlExtras();

  @Pattern("[a-zA-Z0-9_-]*")
  @Override
  public @NotNull String getID() {
    return ID;
  }

  @Override
  public @NotNull FlashUmlVisibilityManager createVisibilityManager() {
    return myVisibilityManager;
  }

  @Override
  public @NotNull FlashUmlNodeContentManager getNodeContentManager() {
    return myNodeContentManager;
  }

  @Override
  public @NotNull DiagramNodeContentManager createNodeContentManager() {
    return new FlashUmlNodeContentManager();
  }

  @Override
  public @NotNull FlashUmlElementManager getElementManager() {
    return myElementManager;
  }

  @Override
  public @NotNull FlashUmlVfsResolver getVfsResolver() {
    return myVfsResolver;
  }

  @Override
  public @NotNull FlashUmlRelationshipManager getRelationshipManager() {
    return myRelationshipManager;
  }

  @Override
  public @NotNull FlashUmlDataModel createDataModel(@NotNull Project project,
                                                    @Nullable Object element,
                                                    @Nullable VirtualFile file,
                                                    @NotNull DiagramPresentationModel presentationModel) {
    if (element instanceof JSFile) {
      element = JSPsiImplUtils.findQualifiedElement((JSFile)element);
    }
    else if (element instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)element)) {
      element = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)element);
    }
    else if (element instanceof PsiDirectory) {
      PsiDirectory directory = (PsiDirectory)element;
      element = JSResolveUtil.getExpectedPackageNameFromFile(directory.getVirtualFile(), directory.getProject());
    }
    return new FlashUmlDataModel(project, element, file, this);
  }

  @Override
  public DiagramScopeManager<Object> createScopeManager(@NotNull Project project) {
    return new DiagramPsiScopeManager<>(project);
  }

  @Override
  public @NotNull DiagramColorManager getColorManager() {
    return myColorManager;
  }

  @NotNull
  @Override
  public FlashUmlExtras getExtras() {
    return myExtras;
  }

  private final DiagramCreateNewElementAction[] myElementActions = {
    new NewActionScriptClassUmlAction(),
    new NewFlexComponentUmlAction()
  };

  private final DiagramCreateNewNodeElementAction[] myNodeActions = {
    new FlexCreateFieldFromDiagramAction(),
    new FlexCreateMethodFromDiagramAction(),
    new FlexCreateConstructorFromDiagramAction()
  };

  @Override
  public DiagramCreateNewElementAction<Object, ?> @NotNull [] getCreateNewActions() {
    //noinspection unchecked
    return myElementActions;
  }

  @Override
  public DiagramCreateNewNodeElementAction<Object, ?> @NotNull [] getCreateNewNodeElementActions() {
    //noinspection unchecked
    return myNodeActions;
  }

  @Override
  public @NotNull String getPresentableName() {
    return FlexBundle.message("js.uml.presentable.name");
  }

  @Override
  public DiagramEdgeCreationPolicy<Object> getEdgeCreationPolicy() {
    return new FlashUmlEdgeCreationPolicy();
  }
}
