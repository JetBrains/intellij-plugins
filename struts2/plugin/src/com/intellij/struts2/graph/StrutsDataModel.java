/*
 * Copyright 2010 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.struts2.graph;

import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.graph.builder.NodesGroup;
import com.intellij.openapi.graph.builder.components.BasicNodesGroup;
import com.intellij.openapi.graph.view.NodeLabel;
import com.intellij.openapi.graph.view.hierarchy.GroupNodeRealizer;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.graph.beans.ActionNode;
import com.intellij.struts2.graph.beans.BasicStrutsEdge;
import com.intellij.struts2.graph.beans.BasicStrutsNode;
import com.intellij.struts2.graph.beans.ResultNode;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Yann C&eacute;bron
 * @author Sergey Vasiliev
 */
public class StrutsDataModel extends GraphDataModel<BasicStrutsNode, BasicStrutsEdge> {

  private final Set<BasicStrutsNode> myNodes = new HashSet<>();
  private final Set<BasicStrutsEdge> myEdges = new HashSet<>();

  private final Map<PsiFile, NodesGroup> myGroups = new HashMap<>();

  private final Project myProject;
  private final XmlFile myFile;

  @NonNls
  private static final String UNKNOWN = "???";

  public StrutsDataModel(final XmlFile file) {
    myFile = file;
    myProject = file.getProject();
  }

  @Override
  @NotNull
  public Collection<BasicStrutsNode> getNodes() {
    refreshDataModel();
    return myNodes;
  }

  @Override
  @NotNull
  public Collection<BasicStrutsEdge> getEdges() {
    return myEdges;
  }

  @Override
  @NotNull
  public BasicStrutsNode getSourceNode(final BasicStrutsEdge edge) {
    return edge.getSource();
  }

  @Override
  @NotNull
  public BasicStrutsNode getTargetNode(final BasicStrutsEdge edge) {
    return edge.getTarget();
  }

  @Override
  @NotNull
  public String getNodeName(final BasicStrutsNode node) {
    return node.getName();
  }

  @Override
  @NotNull
  public String getEdgeName(final BasicStrutsEdge edge) {
    return edge.getName();
  }

  @Override
  public BasicStrutsEdge createEdge(@NotNull final BasicStrutsNode from, @NotNull final BasicStrutsNode to) {
    return null;
  }

  @Override
  public void dispose() {
  }

  private void refreshDataModel() {
    myNodes.clear();
    myEdges.clear();
    updateDataModel();
  }

  @Override
  public NodesGroup getGroup(final BasicStrutsNode basicStrutsNode) {
    if (isGroupElements()) {
      final XmlElement xmlElement = basicStrutsNode.getIdentifyingElement().getXmlElement();
      assert xmlElement != null;
      return myGroups.get(xmlElement.getContainingFile());
    }

    return super.getGroup(basicStrutsNode);
  }

  private void addNode(final BasicStrutsNode node) {
    if (!node.getIdentifyingElement().isValid()) {
      return;
    }

    myNodes.add(node);

    if (isGroupElements()) {
      final XmlElement element = node.getIdentifyingElement().getXmlElement();
      assert element != null;
      final PsiFile file = element.getContainingFile();
      if (file != null && !myGroups.containsKey(file)) {
        final String name = file.getName();

        final BasicNodesGroup group = new BasicNodesGroup(name) {

          @Override
          public @Nullable GroupNodeRealizer createGroupNodeRealizer() {
            final GroupNodeRealizer groupNodeRealizer = super.createGroupNodeRealizer();
            assert groupNodeRealizer != null;

            final NodeLabel nodeLabel = groupNodeRealizer.getLabel();
            nodeLabel.setText("      " + getGroupName());
            nodeLabel.setModel(NodeLabel.INTERNAL);
            nodeLabel.setPosition(NodeLabel.TOP_RIGHT);

            return groupNodeRealizer;
          }
        };

        // collapse all other files
        group.setClosed(file != myFile);

        myGroups.put(file, group);

      }
    }
  }

  // TODO configurable?
  private boolean isGroupElements() {
    return true;
  }

  private void addEdge(final BasicStrutsEdge edge) {
    if (!edge.getSource().getIdentifyingElement().isValid() ||
        !edge.getTarget().getIdentifyingElement().isValid()) {
      return;
    }

    myEdges.add(edge);
  }

  private void updateDataModel() {
    final StrutsModel model = StrutsManager.getInstance(myProject).getModelByFile(myFile);
    if (model == null) {
      return;
    }

    for (final StrutsPackage strutsPackage : model.getStrutsPackages()) {
      for (final Action action : strutsPackage.getActions()) {
        final ActionNode actionNode = new ActionNode(action, action.getName().getStringValue());
        addNode(actionNode);

        for (final Result result : action.getResults()) {
          final PathReference pathReference = result.getValue();
          final String path = pathReference != null ? pathReference.getPath() : UNKNOWN;

          final ResultNode resultNode = new ResultNode(result, path);
          addNode(resultNode);

          final String resultName = result.getName().getStringValue();
          addEdge(new BasicStrutsEdge(actionNode, resultNode, resultName != null ? resultName : Result.DEFAULT_NAME));
        }

      }
    }

  }

}