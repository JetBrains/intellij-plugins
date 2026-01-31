// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.diagram.DiagramDataModel;
import com.intellij.diagram.DiagramEdge;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramProvider;
import com.intellij.jhipster.JdlLanguage;
import com.intellij.jhipster.psi.JdlEntity;
import com.intellij.jhipster.psi.JdlEnum;
import com.intellij.jhipster.psi.JdlEnumValue;
import com.intellij.jhipster.psi.JdlFile;
import com.intellij.jhipster.psi.JdlRelationshipEntity;
import com.intellij.jhipster.psi.JdlRelationshipGroup;
import com.intellij.jhipster.psi.JdlRelationshipMapping;
import com.intellij.jhipster.uml.model.JdlDiagramData;
import com.intellij.jhipster.uml.model.JdlDiagramRootData;
import com.intellij.jhipster.uml.model.JdlEntityNodeData;
import com.intellij.jhipster.uml.model.JdlEntityNodeField;
import com.intellij.jhipster.uml.model.JdlEntityNodeLink;
import com.intellij.jhipster.uml.model.JdlEntityNodeLinkType;
import com.intellij.jhipster.uml.model.JdlEnumNodeData;
import com.intellij.jhipster.uml.model.JdlEnumNodeLink;
import com.intellij.jhipster.uml.model.JdlNodeData;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.jhipster.JdlConstants.USER_ENTITY_NAME;

final class JdlUmlDataModel extends DiagramDataModel<JdlNodeData> {
  private final List<JdlDiagramNode> nodes = new ArrayList<>();
  private final List<DiagramEdge<JdlNodeData>> edges = new ArrayList<>();

  private final JdlNodeData seedData;
  private JdlDiagramData diagramData;

  JdlUmlDataModel(@NotNull Project project,
                  @NotNull DiagramProvider<JdlNodeData> provider, @Nullable JdlNodeData seedData) {
    super(project, provider);
    this.seedData = seedData;
  }

  @Override
  public @NotNull ModificationTracker getModificationTracker() {
    var psiModificationTracker = PsiModificationTracker.getInstance(getProject());
    return psiModificationTracker.forLanguage(JdlLanguage.INSTANCE);
  }

  @Override
  public @NotNull Collection<? extends DiagramNode<JdlNodeData>> getNodes() {
    return nodes;
  }

  @Override
  public @NotNull String getNodeName(@NotNull DiagramNode<JdlNodeData> diagramNode) {
    return diagramNode.getIdentifyingElement().getName();
  }

  @Override
  public @Nullable DiagramNode<JdlNodeData> addElement(@Nullable JdlNodeData data) {
    if (data == null) return null;

    if (data instanceof JdlDiagramRootData) {
      this.diagramData = extractData(getProject(), ((JdlDiagramRootData)data).getVirtualFile());

      var entityMapping = new HashMap<JdlEntityNodeData, DiagramNode<JdlNodeData>>();
      var enumMapping = new HashMap<JdlEnumNodeData, DiagramNode<JdlNodeData>>();

      for (var entity : diagramData.getEntities()) {
        entityMapping.put(entity, addElement(entity));
      }
      for (var enumeration : diagramData.getEnums()) {
        enumMapping.put(enumeration, addElement(enumeration));
      }

      for (JdlEntityNodeLink entityLink : diagramData.getEntityLinks()) {
        var from = entityMapping.get(entityLink.getFromEntity());
        var to = entityMapping.get(entityLink.getToEntity());
        edges.add(new JdlDiagramEntityEdge(from, to, entityLink.getType()));
      }

      for (JdlEnumNodeLink enumLink : diagramData.getEnumLinks()) {
        var from = entityMapping.get(enumLink.getEntity());
        var to = enumMapping.get(enumLink.getEnumeration());
        edges.add(new JdlDiagramEnumEdge(from, to));
      }

      return null;
    }

    var node = new JdlDiagramNode(data, getProvider());
    this.nodes.add(node);
    return node;
  }

  @Override
  public @NotNull Collection<? extends DiagramEdge<JdlNodeData>> getEdges() {
    return edges;
  }

  @Override
  public void dispose() {
  }

  public static @NotNull JdlDiagramData extractData(@NotNull Project project, @Nullable VirtualFile file) {
    if (file == null) return new JdlDiagramData(List.of(), List.of(), List.of(), List.of());

    var psiFile = PsiManager.getInstance(project).findFile(file);
    if (!(psiFile instanceof JdlFile)) return new JdlDiagramData(List.of(), List.of(), List.of(), List.of());

    return extractData((JdlFile)psiFile);
  }

  public static @NotNull JdlDiagramData extractData(@NotNull JdlFile file) {
    var userEntityData = new JdlEntityNodeData(USER_ENTITY_NAME, List.of(
      new JdlEntityNodeField("login", "String", true)
    ));

    Map<String, JdlEntityNodeData> entities = new HashMap<>();
    Map<String, JdlEnumNodeData> enums = new HashMap<>();

    for (var declaration : file.getDeclarations()) {
      if (declaration instanceof JdlEnum enumeration) {
        String name = enumeration.getName();

        if (name != null) {
          List<String> enumItems = ContainerUtil.map(enumeration.getEnumValueList(), JdlEnumValue::getName);
          enums.put(name, new JdlEnumNodeData(name, enumItems));
        }
      }
      else if (declaration instanceof JdlEntity entity) {
        String name = entity.getName();

        if (name != null) {
          List<JdlEntityNodeField> fieldItems = ContainerUtil.map(entity.getEntityFieldMappingList(), field ->
            new JdlEntityNodeField(field.getName(), field.getType(), field.isRequired()));
          entities.put(name, new JdlEntityNodeData(name, fieldItems));
        }
      }
    }

    List<JdlRelationshipGroup> relationships = Arrays.stream(file.getChildren())
      .filter(c -> c instanceof JdlRelationshipGroup)
      .map(c -> ((JdlRelationshipGroup)c))
      .toList();

    List<JdlEntityNodeLink> entityLinks = new ArrayList<>();
    for (JdlRelationshipGroup relationshipGroup : relationships) {
      var type = JdlEntityNodeLinkType.fromString(relationshipGroup.getType());
      if (type == null) continue;

      for (JdlRelationshipMapping mapping : relationshipGroup.getRelationshipMappingList()) {
        var entityPair = mapping.getRelationshipEntityList().stream()
          .map(JdlRelationshipEntity::getId)
          .map(PsiElement::getText)
          .toList();

        if (entityPair.size() == 2) {
          if (USER_ENTITY_NAME.equals(entityPair.get(0)) || USER_ENTITY_NAME.equals(entityPair.get(1))) {
            if (!entities.containsKey(USER_ENTITY_NAME)) {
              entities.put(USER_ENTITY_NAME, userEntityData);
            }
          }

          var leftEntityNode = entities.get(entityPair.get(0));
          var rightEntityNode = entities.get(entityPair.get(1));

          if (leftEntityNode != null && rightEntityNode != null) {
            entityLinks.add(new JdlEntityNodeLink(type, leftEntityNode, rightEntityNode));
          }
        }
      }
    }

    List<JdlEnumNodeLink> enumLinks = new ArrayList<>();

    for (var entityNodeData : entities.values()) {
      for (var property : entityNodeData.getProperties()) {
        var enumNodeData = enums.get(property.getType());

        if (enumNodeData != null) {
          enumLinks.add(new JdlEnumNodeLink(entityNodeData, enumNodeData));
        }
      }
    }

    return new JdlDiagramData(entities.values(), enums.values(), entityLinks, enumLinks);
  }

  @Override
  public void refreshDataModel() {
    if (seedData instanceof JdlDiagramRootData) {
      JdlDiagramData newDiagramData = extractData(
        getProject(),
        ((JdlDiagramRootData)seedData).getVirtualFile()
      );

      if (newDiagramData.equals(diagramData)) return; // nothing changed

      removeAll();

      nodes.clear();
      edges.clear();

      addElement(seedData);
    }
  }
}
