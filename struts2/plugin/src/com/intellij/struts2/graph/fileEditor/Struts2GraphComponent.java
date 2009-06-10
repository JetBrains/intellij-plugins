/*
 * Copyright 2008 The authors
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
package com.intellij.struts2.graph.fileEditor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.Overview;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.graph.StrutsDataModel;
import com.intellij.struts2.graph.StrutsPresentationModel;
import com.intellij.struts2.graph.beans.BasicStrutsEdge;
import com.intellij.struts2.graph.beans.BasicStrutsNode;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.DomEventAdapter;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.events.DomEvent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class Struts2GraphComponent extends JPanel implements DataProvider, Disposable {
  @NonNls
  private static final String STRUTS2_DESIGNER_COMPONENT = "STRUTS2_DESIGNER_COMPONENT";

  @NonNls
  private final Struts2GraphNavigationProvider myNavigationProvider = new Struts2GraphNavigationProvider();

  private final GraphBuilder<BasicStrutsNode, BasicStrutsEdge> myBuilder;

  public Struts2GraphComponent(final XmlFile xmlFile) {
    final Project project = xmlFile.getProject();

    final Graph2D graph = GraphManager.getGraphManager().createGraph2D();
    final Graph2DView view = GraphManager.getGraphManager().createGraph2DView();
    final StrutsDataModel myDataModel = new StrutsDataModel(xmlFile);
    final StrutsPresentationModel presentationModel = new StrutsPresentationModel(graph);

    myBuilder = GraphBuilderFactory.getInstance(project).createGraphBuilder(graph,
                                                                            view,
                                                                            myDataModel,
                                                                            presentationModel);

    setLayout(new BorderLayout());

    add(createToolbarPanel(), BorderLayout.NORTH);
    add(myBuilder.getView().getJComponent(), BorderLayout.CENTER);

    Disposer.register(this, myBuilder);

    myBuilder.initialize();

    DomManager.getDomManager(myBuilder.getProject()).addDomEventListener(new DomEventAdapter() {
      public void eventOccured(final DomEvent event) {
        if (isShowing()) {
          myBuilder.queueUpdate();
        }
      }
    }, this);
  }

  private JComponent createToolbarPanel() {
    final DefaultActionGroup actions = new DefaultActionGroup();
    actions.add(GraphViewUtil.getBasicToolbar(myBuilder));
    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN,
                                                                                        actions,
                                                                                        true);
    return actionToolbar.getComponent();
  }

  public List<DomElement> getSelectedDomElements() {
    final List<DomElement> selected = new ArrayList<DomElement>();
    final Graph2D graph = myBuilder.getGraph();
    for (final Node node : graph.getNodeArray()) {
      if (graph.isSelected(node)) {
        final BasicStrutsNode nodeObject = myBuilder.getNodeObject(node);
        if (nodeObject != null) {
          ContainerUtil.addIfNotNull(nodeObject.getIdentifyingElement(), selected);
        }
      }
    }
    return selected;
  }

  public void setSelectedDomElement(final DomElement domElement) {
    // TODO
    //if (domElement == null) return;
    //
    //final SeamPagesDomElement pageflowDomElement = domElement.getParentOfType(SeamPagesDomElement.class, false);
    //if (pageflowDomElement == null) return;
    //
    //final Node selectedNode = myBuilder.getNode(pageflowDomElement);
    //
    //if (selectedNode != null) {
    //  final Graph2D graph = myBuilder.getGraph();
    //
    //  for (Node n : graph.getNodeArray()) {
    //    final boolean selected = n.equals(selectedNode);
    //    graph.setSelected(n, selected);
    //    if (selected) {
    //      final YRectangle yRectangle = graph.getRectangle(n);
    //      if (!myBuilder.getView().getVisibleRect().contains(
    //        new Rectangle((int)yRectangle.getX(), (int)yRectangle.getY(), (int)yRectangle.getWidth(), (int)yRectangle.getHeight()))) {
    //        myBuilder.getView().setCenter(graph.getX(n), graph.getY(n));
    //      }
    //    }
    //  }
    //}
    //myBuilder.getView().updateView();
  }

  public GraphBuilder getBuilder() {
    return myBuilder;
  }

  public Overview getOverview() {
    return GraphManager.getGraphManager().createOverview(myBuilder.getView());
  }

  public void dispose() {
  }

  private class Struts2GraphNavigationProvider extends DomElementNavigationProvider {

    @NonNls
    private static final String STRUTS2_DESIGNER_NAVIGATION_PROVIDER_NAME = "STRUTS2_DESIGNER_NAVIGATION_PROVIDER_NAME";

    public String getProviderName() {
      return STRUTS2_DESIGNER_NAVIGATION_PROVIDER_NAME;
    }

    public void navigate(final DomElement domElement, final boolean requestFocus) {
      setSelectedDomElement(domElement);
    }

    public boolean canNavigate(final DomElement domElement) {
      return domElement.isValid();
    }
  }

  public Struts2GraphNavigationProvider getNavigationProvider() {
    return myNavigationProvider;
  }

  @Nullable
  public Object getData(@NonNls final String dataId) {
    if (dataId.equals(STRUTS2_DESIGNER_COMPONENT)) {
      return this;
    }

    return null;
  }

}
