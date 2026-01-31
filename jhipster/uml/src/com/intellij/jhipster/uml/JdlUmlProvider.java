// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.diagram.DiagramDataModel;
import com.intellij.diagram.DiagramElementManager;
import com.intellij.diagram.DiagramNodeContentManager;
import com.intellij.diagram.DiagramPresentationModel;
import com.intellij.diagram.DiagramProvider;
import com.intellij.diagram.DiagramRelationshipManager;
import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.diagram.DiagramVisibilityManager;
import com.intellij.diagram.EmptyDiagramVisibilityManager;
import com.intellij.jhipster.JdlBundle;
import com.intellij.jhipster.uml.model.JdlNodeData;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.diagram.DiagramRelationshipManager.NO_RELATIONSHIP_MANAGER;

final class JdlUmlProvider extends DiagramProvider<JdlNodeData> {

  private final DiagramVfsResolver<JdlNodeData> vfsResolver = new JdlUmlVfsResolver();
  private final DiagramElementManager<JdlNodeData> elementManager = new JdlUmlElementManager();

  @Pattern("[a-zA-Z0-9_-]*")
  @Override
  public @NotNull String getID() {
    return "JhipsterJDL";
  }

  @SuppressWarnings("DialogTitleCapitalization")
  @Override
  public @NotNull String getPresentableName() {
    return JdlBundle.message("label.jhipster.entities");
  }

  public JdlUmlProvider() {
    this.elementManager.setUmlProvider(this);
  }

  @Override
  public @NotNull DiagramDataModel<JdlNodeData> createDataModel(@NotNull Project project,
                                                                @Nullable JdlNodeData seedData,
                                                                @Nullable VirtualFile umlVirtualFile,
                                                                @NotNull DiagramPresentationModel diagramPresentationModel) {
    var model = new JdlUmlDataModel(project, this, seedData);
    if (seedData != null) {
      model.addElement(seedData);
    }
    return model;
  }

  @Override
  public @NotNull DiagramVisibilityManager createVisibilityManager() {
    return EmptyDiagramVisibilityManager.INSTANCE;
  }

  @Override
  public @NotNull DiagramElementManager<JdlNodeData> getElementManager() {
    return elementManager;
  }

  @Override
  public @NotNull DiagramVfsResolver<JdlNodeData> getVfsResolver() {
    return vfsResolver;
  }

  @SuppressWarnings("unchecked")
  @Override
  public @NotNull DiagramRelationshipManager<JdlNodeData> getRelationshipManager() {
    return (DiagramRelationshipManager<JdlNodeData>)NO_RELATIONSHIP_MANAGER;
  }

  @Override
  public @NotNull DiagramNodeContentManager createNodeContentManager() {
    return new JdlUmlCategoryManager();
  }
}
