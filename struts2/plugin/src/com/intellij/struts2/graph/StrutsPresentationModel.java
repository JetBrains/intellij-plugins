/*
 * Copyright 2019 The authors
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

import com.intellij.openapi.graph.builder.components.BasicGraphPresentationModel;
import com.intellij.openapi.graph.builder.renderer.BasicGraphNodeRenderer;
import com.intellij.openapi.graph.services.GraphNodeRealizerService;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.graph.beans.BasicStrutsEdge;
import com.intellij.struts2.graph.beans.BasicStrutsNode;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 * @author Sergey Vasiliev
 */
public class StrutsPresentationModel extends BasicGraphPresentationModel<BasicStrutsNode, BasicStrutsEdge> {

  private BasicGraphNodeRenderer myRenderer;

  public StrutsPresentationModel(final Graph2D graph) {
    super(graph);
    getSettings().setShowEdgeLabels(true);
  }

  @Override
  @NotNull
  public NodeRealizer getNodeRealizer(final @Nullable BasicStrutsNode node) {
    return GraphNodeRealizerService.getInstance().createGenericNodeRealizer("Struts2NodeRenderer", getRenderer());
  }

  private BasicGraphNodeRenderer getRenderer() {
    if (myRenderer == null) {
      myRenderer = new StrutsNodeRenderer(getGraphBuilder());
    }
    return myRenderer;
  }

  @Override
  public boolean editNode(final @Nullable BasicStrutsNode node) {
    if (node == null) { // TODO should not happen
      return false;
    }

    final XmlElement xmlElement = node.getIdentifyingElement().getXmlElement();
    if (xmlElement instanceof Navigatable) {
      OpenSourceUtil.navigate((Navigatable)xmlElement);
      return true;
    }
    return super.editNode(node);
  }

  @Override
  public boolean editEdge(final @Nullable BasicStrutsEdge edge) {
    if (edge == null) {
      return false; // TODO should not happen
    }

    final XmlElement xmlElement = edge.getSource().getIdentifyingElement().getXmlElement();
    if (xmlElement instanceof Navigatable) {
      OpenSourceUtil.navigate((Navigatable)xmlElement);
      return true;
    }
    return super.editEdge(edge);
  }

  @Override
  public @Nullable String getNodeTooltip(@Nullable final BasicStrutsNode node) {
    if (node == null) {
      return null;
    }

    final DomElement element = node.getIdentifyingElement();
    if (element instanceof Action action) {
      final StrutsPackage strutsPackage = action.getStrutsPackage();

      final DocumentationBuilder builder = new DocumentationBuilder();
      final PsiClass actionClass = action.searchActionClass();
      builder.addLine("Action", action.getName().getStringValue())
        .addLine("Class", actionClass != null ? actionClass.getQualifiedName() : null)
        .addLine("Method", action.getMethod().getStringValue())
        .addLine("Package", strutsPackage.getName().getStringValue())
        .addLine("Namespace", strutsPackage.searchNamespace());

      return builder.getText();
    }

    if (element instanceof Result result) {
      final PathReference pathReference = result.getValue();
      final String displayPath = pathReference != null ? pathReference.getPath() : "???";
      final ResultType resultType = result.getEffectiveResultType();
      final String resultTypeValue = resultType != null ? resultType.getName().getStringValue() : "???";

      final DocumentationBuilder builder = new DocumentationBuilder();
      builder.addLine("Path", displayPath)
        .addLine("Type", resultTypeValue);
      return builder.getText();
    }

    return null;
  }

  @Override
  public void customizeSettings(final @NotNull Graph2DView view, final @NotNull EditMode editMode) {
    editMode.allowBendCreation(false);
    editMode.allowEdgeCreation(false);

    view.setFitContentOnResize(false);
    view.setAntialiasedPainting(Registry.is(BasicGraphPresentationModel.USE_ANTIALIAING_REGKEY));
    view.setGridVisible(false);
    view.fitContent();
  }

  /**
   * Builds HTML-table based descriptions for use in documentation, tooltips.
   *
   * @author Yann C&eacute;bron
   */
  private static class DocumentationBuilder {

    @NonNls
    private final StringBuilder builder = new StringBuilder("<html><table>");

    /**
     * Adds a labeled content line.
     *
     * @param label   Content description.
     * @param content Content text, {@code null} or empty text will be replaced with '-'.
     * @return this instance.
     */
    private DocumentationBuilder addLine(@NotNull @NonNls final String label, @Nullable @NonNls final String content) {
      builder.append("<tr><td><strong>").append(label).append(":</strong></td>")
        .append("<td>").append(StringUtil.isNotEmpty(content) ? content : "-").append("</td></tr>");
      return this;
    }

    private String getText() {
      builder.append("</table></html>");
      return builder.toString();
    }
  }
}
