// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.diagram;

import com.intellij.diagram.*;
import com.intellij.diagram.components.DiagramNodeContainer;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.diagram.presentation.DiagramState;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.modules.diagram.JSModulesDiagramUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.geom.YPoint;
import com.intellij.openapi.graph.layout.CanonicMultiStageLayouter;
import com.intellij.openapi.graph.layout.Layouter;
import com.intellij.openapi.graph.layout.ParallelEdgeLayouter;
import com.intellij.openapi.graph.layout.organic.SmartOrganicLayouter;
import com.intellij.openapi.graph.services.GraphExportService;
import com.intellij.openapi.graph.services.GraphLayoutService;
import com.intellij.openapi.graph.services.GraphSelectionService;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uml.UmlGraphBuilder;
import com.intellij.uml.core.renderers.DefaultUmlRenderer;
import com.intellij.uml.presentation.DiagramPresentationModelImpl;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;
import org.angularjs.AngularJSBundle;
import org.angularjs.codeInsight.router.*;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.StrokeBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.*;

final class AngularUiRouterDiagramProvider extends BaseDiagramProvider<DiagramObject> {
  public static final String ANGULAR_UI_ROUTER = "Angular-ui-router";
  public static final BasicStroke DOTTED_STROKE =
    new BasicStroke(0.7f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{2, 2}, 0.0f);
  public static final StrokeBorder WARNING_BORDER = new StrokeBorder(DOTTED_STROKE, JBColor.red);
  public static final Border ERROR_BORDER = JBUI.Borders.customLine(JBColor.red);
  public static final Border NORMAL_BORDER = JBUI.Borders.customLine(Gray._190);

  private final AbstractDiagramElementManager<DiagramObject> myElementManager;

  public AngularUiRouterDiagramProvider() {
    myElementManager = new AbstractDiagramElementManager<>() {
      @Override
      public Object @NotNull [] getNodeItems(DiagramObject parent) {
        return ArrayUtil.toObjectArray(parent.getChildrenList());
      }

      @Override
      public @Nullable DiagramObject findInDataContext(@NotNull DataContext context) {
        //todo ?
        return null;
      }

      @Override
      public boolean isAcceptableAsNode(@Nullable Object element) {
        return element instanceof DiagramObject;
      }

      @Override
      public String getElementTitle(DiagramObject element) {
        return element.getName();
      }

      @Override
      public @Nullable SimpleColoredText getItemName(@Nullable Object element, @NotNull DiagramState presentation) {
        if (element instanceof DiagramObject) {
          return new SimpleColoredText(((DiagramObject)element).getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        return null;
      }

      @Override
      public @Nullable @Nls String getNodeTooltip(DiagramObject element) {
        final List<String> errors = element.getErrors();
        final List<String> warnings = element.getWarnings();
        final List<String> notes = element.getNotes();
        if (errors.isEmpty() && warnings.isEmpty() && notes.isEmpty()) return element.getTooltip();
        @Nls final StringBuilder sb = new StringBuilder(element.getTooltip());
        if (!notes.isEmpty()) {
          for (String note : notes) {
            sb.append('\n').append(note);
          }
        }
        sb.append("<font style=\"color:#ff0000;\">");
        if (!errors.isEmpty()) {
          sb.append('\n').append(AngularJSBundle.icuMessage("angularjs.ui.router.diagram.node.tooltip.errors", errors.size()))
            .append(":\n");
          for (String error : errors) {
            sb.append(error).append('\n');
          }
        }
        if (!warnings.isEmpty()) {
          sb.append('\n').append(AngularJSBundle.icuMessage("angularjs.ui.router.diagram.node.tooltip.warnings", errors.size()))
            .append(":\n");
          for (String warning : warnings) {
            sb.append(warning).append('\n');
          }
        }
        sb.append("</font>");
        return sb.toString();
      }

      @Override
      public @Nullable Icon getItemIcon(@Nullable Object element, @NotNull DiagramState presentation) {
        return null; //do not show icons
      }
    };
  }

  @Override
  public @NotNull DiagramColorManager getColorManager() {
    return ApplicationManager.getApplication().getService(AngularUiRouterDiagramColorManager.class);
  }

  @Pattern("[a-zA-Z0-9_-]*")
  @Override
  public @NotNull String getID() {
    return ANGULAR_UI_ROUTER;
  }

  @Override
  public @NotNull DiagramElementManager<DiagramObject> getElementManager() {
    return myElementManager;
  }

  @Override
  public @NotNull DiagramVfsResolver<DiagramObject> getVfsResolver() {
    return ApplicationManager.getApplication().getService(AngularUiRouterDiagramVfsResolver.class);
  }

  @Override
  public @NotNull String getPresentableName() {
    return AngularJSBundle.message("angularjs.ui.router.diagram.provider.name");
  }

  @Override
  public @NotNull DiagramDataModel<DiagramObject> createDataModel(@NotNull Project project,
                                                                  @Nullable DiagramObject element,
                                                                  @Nullable VirtualFile file,
                                                                  @NotNull DiagramPresentationModel presentationModel) {
    if (element == null || element.getNavigationTarget() == null) return null;
    final VirtualFile virtualFile = element.getNavigationTarget().getContainingFile().getVirtualFile();
    final AngularUiRouterGraphBuilder.GraphNodesBuilder nodesBuilder =
      AngularUiRouterProviderContext.getInstance(project).getBuilder(virtualFile);
    if (nodesBuilder == null) {
      return new AngularUiRouterDiagramModel(project, virtualFile, this, Collections.emptyList(), Collections.emptyList());
    }
    return new AngularUiRouterDiagramModel(project, virtualFile, this, nodesBuilder.getAllNodes(), nodesBuilder.getEdges());
  }

  @Override
  public @NotNull DiagramPresentationModel createPresentationModel(@NotNull Project project, @NotNull Graph2D graph) {
    return new DiagramPresentationModelImpl(graph, project, this) {
      @Override
      public boolean allowChangeVisibleCategories() {
        return false;
      }

      private final Map<DiagramEdge, EdgeRealizer> myEdgeRealizers = new HashMap<>();

      @Override
      public @NotNull EdgeRealizer getEdgeRealizer(DiagramEdge edge) {
        if (!(edge instanceof AngularUiRouterEdge)) return super.getEdgeRealizer(edge);
        if (myEdgeRealizers.containsKey(edge)) return myEdgeRealizers.get(edge);
        UmlGraphBuilder builder = (UmlGraphBuilder)graph.getDataProvider(DiagramDataKeys.GRAPH_BUILDER_OLD).get(null);
        final Edge graphEdge = builder.getEdge(edge);
        final AngularEdgeLayouter.OneEdgeLayouter layouter =
          new AngularEdgeLayouter.OneEdgeLayouter(graphEdge, (AngularUiRouterEdge)edge, graph);
        layouter.calculateEdgeLayout();
        final QuadCurveEdgeRealizer realizer = layouter.getRealizer();
        for (int i = 0; i < realizer.labelCount(); i++) {
          realizer.removeLabel(realizer.getLabel(i));
        }
        /*final EdgeLabel[] labels = getEdgeLabels(edge, "");
        if (labels.length == 0) realizer.setLabelText("");
        else {
          for (EdgeLabel label : labels) {
            realizer.addLabel(label);
          }
        }*/
        myEdgeRealizers.put(edge, realizer);
        return realizer;
      }

      private final Map<Integer, Integer> myEdgesPositions = new HashMap<>();
      private final Set<AngularUiRouterEdge> myVisibleEdges = new HashSet<>();

      @Override
      public EdgeLabel @NotNull [] getEdgeLabels(@Nullable DiagramEdge umlEdge, @NotNull String label) {
        if (!(umlEdge instanceof AngularUiRouterEdge)) return super.getEdgeLabels(umlEdge, label);
        AngularUiRouterEdge angularEdge = (AngularUiRouterEdge)umlEdge;
        if (!getSettings().isShowEdgeLabels() || StringUtil.isEmptyOrSpaces(angularEdge.getLabel())) {
          return EMPTY_LABELS;
        }
        //if (!myVisibleEdges.contains(umlEdge)) return EMPTY_LABELS;
        UmlGraphBuilder builder = (UmlGraphBuilder)graph.getDataProvider(DiagramDataKeys.GRAPH_BUILDER_OLD).get(null);
        final Edge edge = builder.getEdge(umlEdge);
        final EdgeRealizer edgeRealizer = getEdgeRealizer(umlEdge);
        for (int i = 0; i < edgeRealizer.labelCount(); i++) {
          edgeRealizer.removeLabel(edgeRealizer.getLabel(i));
        }

        final Integer position = calculatePosition(edge, builder);
        final EdgeLabel edgeLabel = GraphManager.getGraphManager().createEdgeLabel();
        final SmartEdgeLabelModel model = GraphManager.getGraphManager().createSmartEdgeLabelModel();
        edgeLabel.setLabelModel(model, model.createDiscreteModelParameter(position));
        edgeLabel.setFontSize(9);
        edgeLabel.setDistance(5);
        edgeLabel.setTextColor(JBColor.foreground());
        myEdgesPositions.put(edge.index(), 1);
        return new EdgeLabel[]{edgeLabel};
      }

      private Integer calculatePosition(final Edge edge, UmlGraphBuilder builder) {
        final Integer existing = myEdgesPositions.get(edge.index());
        if (existing != null) return existing;

        final List<Edge> list = new ArrayList<>();
        for (Edge current : edge.getGraph().getEdgeArray()) {
          if (current.source().index() == edge.source().index() && current.target().index() == edge.target().index() ||
              current.target().index() == edge.source().index() && current.source().index() == edge.target().index()) {
            list.add(current);
          }
        }
        boolean sourceHeavier = edge.source().degree() > edge.target().degree();
        list.sort((o1, o2) -> {
          final YPoint s1 = ((Graph2D)o1.getGraph()).getSourcePointAbs(o1);
          final YPoint s2 = ((Graph2D)o1.getGraph()).getSourcePointAbs(o2);
          if (Math.abs(s1.getX() - s2.getX()) > 5) return Double.compare(s1.getX(), s2.getX());
          return Double.compare(s1.getY(), s2.getY());
        });
        int[] variants = sourceHeavier ? new int[]{
          SmartEdgeLabelModel.POSITION_TARGET_RIGHT,
          SmartEdgeLabelModel.POSITION_RIGHT,
          SmartEdgeLabelModel.POSITION_SOURCE_RIGHT} : new int[]{
          SmartEdgeLabelModel.POSITION_SOURCE_RIGHT,
          SmartEdgeLabelModel.POSITION_RIGHT,
          SmartEdgeLabelModel.POSITION_TARGET_RIGHT};
        int variantIdx = 0;
        for (Edge current : list) {
          myEdgesPositions.put(current.index(), variants[variantIdx++]);
          if (variantIdx >= variants.length) variantIdx = 0;
        }
        return myEdgesPositions.get(edge.index());
      }

      private boolean inUpdate = false;

      @Override
      public void update() {
        if (inUpdate) return;
        try {
          inUpdate = true;
          myEdgeRealizers.clear();
          final List<DiagramNode<?>> nodes = GraphSelectionService.getInstance().getSelectedModelNodes(getGraphBuilder());
          super.update();
          myEdgesPositions.clear();
          final DiagramBuilder builder = getBuilder();
          builder.relayout();
          builder.getView().fitContent();
          builder.queryUpdate().run();
          if (!nodes.isEmpty()) {
            final Collection<DiagramNode<?>> objects = builder.getNodeObjects();
            for (DiagramNode<?> object : objects) {
              if (isInSelectedNodes(nodes, object)) {
                builder.getGraph().setSelected(builder.getNode(object), true);
              }
            }
          }
          updateBySelection(nodes.isEmpty() ? null : nodes.get(0));
        }
        finally {
          inUpdate = false;
        }
      }

      @Override
      public void customizeSettings(@NotNull Graph2DView view, @NotNull EditMode editMode) {
        super.customizeSettings(view, editMode);
        view.getGraph2D().addGraph2DSelectionListener(new Graph2DSelectionListener() {
          @Override
          public void onGraph2DSelectionEvent(Graph2DSelectionEvent _e) {
            myEdgesPositions.clear();
            updateBySelection(null);
            view.updateView();
          }
        });
        view.getJComponent().addComponentListener(new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent e) {
            ApplicationManager.getApplication().invokeLater(() -> {
              UmlGraphBuilder builder = (UmlGraphBuilder)graph.getDataProvider(DiagramDataKeys.GRAPH_BUILDER_OLD).get(null);
              builder.getPresentationModel().update();
            });
          }
        });
        ApplicationManager.getApplication().invokeLater(() -> {
          UmlGraphBuilder builder = (UmlGraphBuilder)graph.getDataProvider(DiagramDataKeys.GRAPH_BUILDER_OLD).get(null);
          final AngularUiRouterDiagramModel model = (AngularUiRouterDiagramModel)builder.getDataModel();
          final AngularUiRouterNode rootNode = findDataObject(project, model).getRootNode();
          updateBySelection(rootNode);
        });
      }

      private void updateBySelection(DiagramNode node) {
        myVisibleEdges.clear();
        UmlGraphBuilder builder = (UmlGraphBuilder)graph.getDataProvider(DiagramDataKeys.GRAPH_BUILDER_OLD).get(null);
        final List<DiagramNode<?>> nodes = new ArrayList<>(GraphSelectionService.getInstance().getSelectedModelNodes(builder));
        if (node != null && !nodes.contains(node)) nodes.add(node);
        DiagramNode selected = null;
        for (DiagramEdge edge : builder.getEdgeObjects()) {
          if (nodes.contains(edge.getSource())) {
            selected = edge.getSource();
          }
          else if (nodes.contains(edge.getTarget())) {
            selected = edge.getTarget();
          }
          else {
            continue;
          }
          break;
        }
        if (selected == null) {
          for (DiagramEdge<?> edge : builder.getEdgeObjects()) {
            if (isInSelectedNodes(nodes, edge.getSource())) {
              selected = edge.getSource();
            }
            else if (isInSelectedNodes(nodes, edge.getTarget())) {
              selected = edge.getTarget();
            }
            else {
              continue;
            }
            break;
          }
        }
        for (DiagramEdge edge : builder.getEdgeObjects()) {
          if (!(edge instanceof AngularUiRouterEdge)) continue;
          if (getSettings().isShowEdgeLabels() &&
              selected != null &&
              (selected.equals(edge.getSource()) || selected.equals(edge.getTarget()))) {
            myVisibleEdges.add((AngularUiRouterEdge)edge);
            graph.setLabelText(builder.getEdge(edge), ((AngularUiRouterEdge)edge).getLabel());
          }
          else {
            graph.setLabelText(builder.getEdge(edge), "");
          }
        }
      }

      @Override
      protected @NotNull DefaultUmlRenderer createRenderer() {
        return new DefaultUmlRenderer(getBuilder(), getModificationTrackerOfViewUpdates()) {
          @Override
          public void tuneNode(NodeRealizer realizer, JPanel wrapper) {
            wrapper.setBorder(JBUI.Borders.empty());
            if (wrapper.getParent() instanceof JComponent) ((JComponent)wrapper.getParent()).setBorder(JBUI.Borders.empty());
            super.tuneNode(realizer, wrapper);
          }
        };
      }
    };
  }

  private static boolean isInSelectedNodes(List<DiagramNode<?>> nodes, DiagramNode<?> node) {
    for (DiagramNode<?> diagramNode : nodes) {
      if (!(node instanceof AngularUiRouterNode && diagramNode instanceof AngularUiRouterNode)) continue;
      final DiagramObject selected = (DiagramObject)diagramNode.getIdentifyingElement();
      final DiagramObject object = (DiagramObject)node.getIdentifyingElement();
      if (selected.getType().equals(object.getType()) && selected.getName().equals(object.getName()) &&
          selected.getNavigationTarget() != null && object.getNavigationTarget() != null &&
          selected.getNavigationTarget().getVirtualFile().equals(object.getNavigationTarget().getVirtualFile())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public @NotNull DiagramExtras<DiagramObject> getExtras() {
    return new DiagramExtras<>() {
      @Override
      public @NotNull List<AnAction> getExtraActions() {
        return Collections.singletonList(new MyEditSourceAction());
      }

      @Override
      public @Nullable Object getData(@NotNull String dataId,
                                      @NotNull List<DiagramNode<DiagramObject>> list,
                                      @NotNull DiagramBuilder builder) {
        if (CommonDataKeys.PSI_ELEMENT.is(dataId) && list.size() == 1) {
          final SmartPsiElementPointer target = list.get(0).getIdentifyingElement().getNavigationTarget();
          return target == null ? null : target.getElement();
        }
        else if (JSModulesDiagramUtils.DIAGRAM_BUILDER.is(dataId)) {
          return builder;
        }
        return null;
      }

      @Override
      public @NotNull Layouter getCustomLayouter(GraphSettings settings, Project project) {
        final SmartOrganicLayouter layouter = GraphLayoutService.getInstance().getOrganicLayouter();
        layouter.setNodeEdgeOverlapAvoided(true);

        layouter.setNodeSizeAware(true);
        layouter.setMinimalNodeDistance(60);
        layouter.setNodeOverlapsAllowed(false);
        layouter.setSmartComponentLayoutEnabled(true);
        layouter.setConsiderNodeLabelsEnabled(true);
        layouter.setDeterministic(true);

        final List<CanonicMultiStageLayouter> list = new ArrayList<>();
        list.add(layouter);
        list.add(GraphLayoutService.getInstance().getBalloonLayouter());
        list.add(GraphLayoutService.getInstance().getCircularLayouter());
        list.add(GraphLayoutService.getInstance().getDirectedOrthogonalLayouter());
        //list.add(settings.getGroupLayouter());
        list.add(GraphLayoutService.getInstance().getHVTreeLayouter());
        //list.add(settings.getChannelLayouter());
        for (CanonicMultiStageLayouter current : list) {
          final ParallelEdgeLayouter parallelEdgeLayouter = GraphManager.getGraphManager().createParallelEdgeLayouter();
          parallelEdgeLayouter.setLineDistance(40);
          parallelEdgeLayouter.setUsingAdaptiveLineDistances(false);
          current.appendStage(parallelEdgeLayouter);
          current.setParallelEdgeLayouterEnabled(false);
        }

        return layouter;
      }

      @Override
      public @NotNull JComponent createNodeComponent(
        @NotNull DiagramNode<DiagramObject> node,
        @NotNull DiagramBuilder builder,
        @NotNull NodeRealizer nodeRealizer,
        @NotNull JPanel wrapper
      ) {
        final DiagramNodeContainer container = new DiagramNodeContainer(node, builder, nodeRealizer);
        if (!GraphExportService.getInstance().isPrintMode()) {
          if (!node.getIdentifyingElement().getErrors().isEmpty()) {
            container.setBorder(ERROR_BORDER);
          }
          else if (!node.getIdentifyingElement().getWarnings().isEmpty()) {
            container.setBorder(WARNING_BORDER);
          }
          else {
            container.setBorder(NORMAL_BORDER);
          }
        }
        if (!node.getIdentifyingElement().getChildrenList().isEmpty()) {
          container.getHeader().setBorder(JBUI.Borders.customLine(Gray._190, 0, 0, 1, 0));
        }
        return container;
      }
    };
  }

  private static AngularUiRouterGraphBuilder.GraphNodesBuilder findDataObject(Project project, final AngularUiRouterDiagramModel model) {
    final Collection<AngularUiRouterNode> nodes = model.getNodes();
    for (AngularUiRouterNode node : nodes) {
      if (Type.topLevelTemplate.equals(node.getIdentifyingElement().getType())) {
        final VirtualFile rootFile = node.getIdentifyingElement().getNavigationTarget().getContainingFile().getVirtualFile();
        return AngularUiRouterProviderContext.getInstance(project).getBuilder(rootFile);
      }
    }
    return null;
  }

  private static class MyEditSourceAction extends AnAction {
    private final AnAction myAction;

    MyEditSourceAction() {
      super(AngularJSBundle.message("angularjs.ui.router.diagram.action.edit.source.name"),
            AngularJSBundle.message("angularjs.ui.router.diagram.action.edit.source.description"),
            AllIcons.Actions.EditSource);
      myAction = ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      final Project project = e.getData(CommonDataKeys.PROJECT);
      if (project == null) {
        e.getPresentation().setEnabled(false);
        return;
      }
      final List<DiagramNode<?>> nodes = JSModulesDiagramUtils.getSelectedNodes(e);
      e.getPresentation().setEnabled(nodes != null && nodes.size() == 1 && nodes.get(0) instanceof AngularUiRouterNode);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      final Project project = e.getData(CommonDataKeys.PROJECT);
      if (project == null) {
        return;
      }
      final List<DiagramNode<?>> nodes = JSModulesDiagramUtils.getSelectedNodes(e);
      if (nodes == null || nodes.size() != 1 || !(nodes.get(0) instanceof AngularUiRouterNode)) {
        return;
      }

      final AngularUiRouterNode node = (AngularUiRouterNode)nodes.get(0);
      final DiagramObject main = node.getIdentifyingElement();
      final List<DiagramObject> childrenList = main.getChildrenList();
      if (childrenList.isEmpty()) {
        myAction.actionPerformed(e);
      }
      else {
        final List<Trinity<String, SmartPsiElementPointer, Icon>> children = ContainerUtil
          .map(childrenList, ch -> Trinity.create(ch.getType().name() + ": " + ch.getName(), ch.getNavigationTarget(), null));
        JSModulesDiagramUtils.showMembersSelectionPopup(
          main.getType().name() + ": " + main.getName(), //NON-NLS
          main.getNavigationTarget(), null, children, e.getDataContext());
      }
    }
  }
}
