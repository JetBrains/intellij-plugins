package org.angularjs.codeInsight.router;

import com.intellij.diagram.*;
import com.intellij.diagram.components.DiagramNodeBodyComponent;
import com.intellij.diagram.components.DiagramNodeContainer;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.diagram.presentation.DiagramState;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.GraphUtil;
import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.geom.YPoint;
import com.intellij.openapi.graph.layout.Layouter;
import com.intellij.openapi.graph.layout.ParallelEdgeLayouter;
import com.intellij.openapi.graph.layout.organic.SmartOrganicLayouter;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.ui.JBColor;
import com.intellij.ui.LightColors;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uml.UmlGraphBuilder;
import com.intellij.uml.presentation.DiagramPresentationModelImpl;
import com.intellij.util.ArrayUtil;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class AngularUiRouterDiagramProvider extends BaseDiagramProvider<DiagramObject> {
  public static final String ANGULAR_UI_ROUTER = "Angular-ui-router";
  private DiagramVfsResolver<DiagramObject> myResolver;
  private AbstractDiagramElementManager<DiagramObject> myElementManager;
  private DiagramColorManagerBase myColorManager;

  private final Map<VirtualFile, AngularUiRouterGraphBuilder.GraphNodesBuilder> myData;
  private Map<AngularUiRouterNode, DiagramNodeBodyComponent> myNodeBodiesMap = new HashMap<>();

  public AngularUiRouterDiagramProvider() {
    myData = new HashMap<>();
    myResolver = new DiagramVfsResolver<DiagramObject>() {
      @Override
      public String getQualifiedName(DiagramObject element) {
        if ((Type.template.equals(element.getType()) || Type.topLevelTemplate.equals(element.getType())) &&
            element.getNavigationTarget() != null) {
          final PsiFile psiFile = element.getNavigationTarget().getContainingFile();
          return psiFile == null ? "" : psiFile.getVirtualFile().getPath();
        }
        else {
          return "";
        }
      }

      @Nullable
      @Override
      public DiagramObject resolveElementByFQN(String fqn, Project project) {
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(fqn);
        if (file == null) {
          return null;
        }
        else {
          final AngularUiRouterGraphBuilder.GraphNodesBuilder builder = myData.get(file);
          return builder == null ? null : builder.getRootNode().getIdentifyingElement();
        }
      }
    };
    myElementManager = new AbstractDiagramElementManager<DiagramObject>() {
      @Override
      public Object[] getNodeItems(DiagramObject parent) {
        return ArrayUtil.toObjectArray(parent.getChildrenList());
      }

      @Nullable
      @Override
      public DiagramObject findInDataContext(DataContext context) {
        //todo ?
        return null;
      }

      @Override
      public boolean isAcceptableAsNode(Object element) {
        return element instanceof DiagramObject;
      }

      @Nullable
      @Override
      public String getElementTitle(DiagramObject element) {
        return element.getName();
      }

      @Nullable
      @Override
      public SimpleColoredText getItemName(Object element, DiagramState presentation) {
        if (element instanceof DiagramObject) {
          return new SimpleColoredText(((DiagramObject)element).getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        return null;
      }

      @Override
      public String getNodeTooltip(DiagramObject element) {
        return element.getTooltip();
      }

      @Override
      public Icon getItemIcon(Object element, DiagramState presentation) {
        return null; //do not show icons
      }
    };
    myColorManager = new DiagramColorManagerBase() {
      @Override
      public Color getNodeHeaderColor(DiagramBuilder builder, @Nullable DiagramNode node) {
        return getColor(node.getIdentifyingElement());
      }

      @Override
      public Color getNodeBackground(Project project, Object nodeElement, boolean selected) {
        return getColor(nodeElement);
      }

      @Nullable
      private Color getColor(Object nodeElement) {
        if (nodeElement instanceof DiagramObject) {
          final DiagramObject element = ((DiagramObject)nodeElement);
          if (Type.state.equals(element.getType())) {
            return LightColors.YELLOW;
          }
          else if (Type.view.equals(element.getType())) {
            return LightColors.BLUE;
          }
          else if (Type.template.equals(element.getType())) {
            return LightColors.GREEN;
          }
          else if (Type.templatePlaceholder.equals(element.getType())) {
            return LightColors.SLIGHTLY_GREEN;
          }
        }
        return null;
      }
    };
  }

  public void reset() {
    myData.clear();
  }

  public void registerNodesBuilder(AngularUiRouterGraphBuilder.GraphNodesBuilder nodesBuilder) {
    myData.put(nodesBuilder.getKey(), nodesBuilder);
  }

  @Override
  public DiagramColorManager getColorManager() {
    return myColorManager;
  }

  @Pattern("[a-zA-Z0-9_-]*")
  @Override
  public String getID() {
    return ANGULAR_UI_ROUTER;
  }

  @Override
  public DiagramElementManager<DiagramObject> getElementManager() {
    return myElementManager;
  }

  @Override
  public DiagramVfsResolver<DiagramObject> getVfsResolver() {
    return myResolver;
  }

  @Override
  public String getPresentableName() {
    return "Angular ui-router states and views";
  }

  private static final DiagramCategory[] CATEGORIES =
    new DiagramCategory[]{Type.Categories.STATE, Type.Categories.VIEW, Type.Categories.TEMPLATE, Type.Categories.TEMPLATE_PLACEHOLDER,
      Type.Categories.TOP_LEVEL_TEMPLATE};

  @Override
  public DiagramNodeContentManager getNodeContentManager() {
    return new DiagramNodeContentManager() {

      @Override
      public boolean isInCategory(Object element, DiagramCategory category, DiagramState presentation) {
        if (element instanceof DiagramObject) {
          return ((DiagramObject)element).getType().getCategory().equals(category);
        }
        return false;
      }

      @Override
      public DiagramCategory[] getContentCategories() {
        return CATEGORIES;
      }
    };
  }

  @Override
  public DiagramRelationshipManager<DiagramObject> getRelationshipManager() {
    return new DiagramRelationshipManager<DiagramObject>() {
      @Nullable
      @Override
      public DiagramRelationshipInfo getDependencyInfo(DiagramObject e1, DiagramObject e2, DiagramCategory category) {
        return null;
      }

      @Override
      public DiagramCategory[] getContentCategories() {
        return CATEGORIES;
      }
    };
  }

  @Override
  public DiagramDataModel<DiagramObject> createDataModel(@NotNull Project project,
                                                         @Nullable DiagramObject element,
                                                         @Nullable VirtualFile file,
                                                         DiagramPresentationModel presentationModel) {
    if (element == null || element.getNavigationTarget() == null) return null;
    final VirtualFile virtualFile = element.getNavigationTarget().getContainingFile().getVirtualFile();
    final AngularUiRouterGraphBuilder.GraphNodesBuilder nodesBuilder = myData.get(virtualFile);
    if (nodesBuilder == null) return null;
    return new AngularUiRouterDiagramModel(project, this, nodesBuilder.getAllNodes(), nodesBuilder.getEdges());
  }

  @Nullable
  @Override
  public DiagramPresentationModel createPresentationModel(Project project, Graph2D graph) {
    return new DiagramPresentationModelImpl(graph, project, this) {
      @NotNull
      @Override
      public EdgeRealizer getEdgeRealizer(DiagramEdge edge) {
        UmlGraphBuilder builder = (UmlGraphBuilder)graph.getDataProvider(DiagramDataKeys.GRAPH_BUILDER).get(null);
        final Edge graphEdge = builder.getEdge(edge);
        final AngularEdgeLayouter.OneEdgeLayouter layouter =
          new AngularEdgeLayouter.OneEdgeLayouter(graphEdge, (AngularUiRouterEdge)edge, graph);
        layouter.calculateEdgeLayout();

        return layouter.getRealizer();
      }

      private Map<Integer, Integer> myEdgesPositions = new HashMap<>();

      @Override
      public EdgeLabel[] getEdgeLabels(DiagramEdge umlEdge, String label) {
        AngularUiRouterEdge angularEdge = (AngularUiRouterEdge)umlEdge;
        if ( !isShowEdgeLabels() || umlEdge == null || StringUtil.isEmptyOrSpaces(angularEdge.getLabel())) {
          return EMPTY_LABELS;
        }
        UmlGraphBuilder builder = (UmlGraphBuilder)graph.getDataProvider(DiagramDataKeys.GRAPH_BUILDER).get(null);
        final Edge edge = builder.getEdge(umlEdge);
        final Integer position = calculatePosition(edge, builder);
        EdgeLabel edgeLabel = GraphManager.getGraphManager().createEdgeLabel();
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
        boolean isSourceNearSelected = false;
        final List<DiagramNode> nodes = new ArrayList<>(GraphUtil.getSelectedNodes(builder));
        for (DiagramNode node : nodes) {
          final int index = builder.getNode(node).index();
          if (index == edge.source().index()) {
            isSourceNearSelected = true;
            break;
          }
          if (index == edge.target().index()) {
            break;
          }
        }
        Collections.sort(list, (o1, o2) -> {
          final YPoint s1 = ((Graph2D)o1.getGraph()).getSourcePointAbs(o1);
          final YPoint s2 = ((Graph2D)o1.getGraph()).getSourcePointAbs(o2);
          if (Math.abs(s1.getX() - s2.getX()) > 5) return Double.compare(s1.getX(), s2.getX());
          return Double.compare(s1.getY(), s2.getY());
        });
        final int[] variants = isSourceNearSelected ? new int[]{SmartEdgeLabelModel.POSITION_TARGET_RIGHT,
          SmartEdgeLabelModel.POSITION_RIGHT, SmartEdgeLabelModel.POSITION_SOURCE_RIGHT} :
                               new int[]{SmartEdgeLabelModel.POSITION_SOURCE_RIGHT,
                                 SmartEdgeLabelModel.POSITION_RIGHT, SmartEdgeLabelModel.POSITION_TARGET_RIGHT};
        int variantIdx = 0;
        for (Edge current : list) {
          myEdgesPositions.put(current.index(), variants[variantIdx++]);
          if (variantIdx >= variants.length) variantIdx = 0;
        }
        return myEdgesPositions.get(edge.index());
      }

      @Override
      public void customizeSettings(Graph2DView view, EditMode editMode) {
        super.customizeSettings(view, editMode);
        view.getGraph2D().addGraph2DSelectionListener(new Graph2DSelectionListener() {
          @Override
          public void onGraph2DSelectionEvent(Graph2DSelectionEvent _e) {
            updateBySelection(null);
            myEdgesPositions.clear();
            view.updateView();
          }
        });
        /*UmlGraphBuilder builder = (UmlGraphBuilder)graph.getDataProvider(DiagramDataKeys.GRAPH_BUILDER).get(null);
        final AngularUiRouterDiagramModel model = (AngularUiRouterDiagramModel)builder.getDataModel();
        final AngularUiRouterNode rootNode = findDataObject(model).getRootNode();
        updateBySelection(rootNode);*/
      }

      private void updateBySelection(DiagramNode node) {
        UmlGraphBuilder builder = (UmlGraphBuilder)graph.getDataProvider(DiagramDataKeys.GRAPH_BUILDER).get(null);
        final List<DiagramNode> nodes = new ArrayList<>(GraphUtil.getSelectedNodes(builder));
        if (node != null) nodes.add(node);
        for (DiagramEdge edge : builder.getEdgeObjects()) {
          if (nodes.contains(edge.getSource()) || nodes.contains(edge.getTarget())) {
            graph.setLabelText(builder.getEdge(edge), ((AngularUiRouterEdge) edge).getLabel());
          } else {
            graph.setLabelText(builder.getEdge(edge), "");
          }
        }
      }
    };
  }

  @NotNull
  @Override
  public DiagramExtras<DiagramObject> getExtras() {
    return new DiagramExtras<DiagramObject>() {
      @Override
      public List<AnAction> getExtraActions() {
        return Collections.singletonList(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
      }

      @Nullable
      @Override
      public Object getData(String dataId, List<DiagramNode<DiagramObject>> list, DiagramBuilder builder) {
        if (CommonDataKeys.PSI_ELEMENT.is(dataId) && list.size() == 1) {
          final SmartPsiElementPointer target = list.get(0).getIdentifyingElement().getNavigationTarget();
          return target == null ? null : target.getElement();
        }
        return null;
      }

      @Nullable
      @Override
      public Layouter getCustomLayouter(Graph2D graph, Project project) {
        final GraphSettingsProvider settingsProvider = GraphSettingsProvider.getInstance(project);
        final GraphSettings settings = settingsProvider.getSettings(graph);

        final SmartOrganicLayouter layouter = settings.getOrganicLayouter();
        layouter.setNodeEdgeOverlapAvoided(true);

        layouter.setNodeSizeAware(true);
        layouter.setMinimalNodeDistance(60);
        layouter.setNodeOverlapsAllowed(false);
        layouter.setSmartComponentLayoutEnabled(true);
        layouter.setConsiderNodeLabelsEnabled(true);

        final ParallelEdgeLayouter parallelEdgeLayouter = GraphManager.getGraphManager().createParallelEdgeLayouter();
        parallelEdgeLayouter.setLineDistance(40);
        parallelEdgeLayouter.setUsingAdaptiveLineDistances(false);
        layouter.appendStage(parallelEdgeLayouter);
        layouter.setParallelEdgeLayouterEnabled(false);
        //final SplitEdgeLayoutStage splitEdgeLayoutStage = GraphManager.getGraphManager().createSplitEdgeLayoutStage();
        //((CanonicMultiStageLayouter) layouter).appendStage(splitEdgeLayoutStage);

        // todo try hierarchical group
        return layouter;
        //uncomment when ready
        //return new AngularJSDiagramLayouter(project, graph, layouter, myData, myNodeBodiesMap);
      }

      @NotNull
      @Override
      public JComponent createNodeComponent(DiagramNode<DiagramObject> node, DiagramBuilder builder, Point basePoint, JPanel wrapper) {
        final DiagramNodeContainer container = new DiagramNodeContainer(node, builder, basePoint);
        myNodeBodiesMap.put((AngularUiRouterNode)node, container.getNodeBodyComponent());
        return container;
      }
    };
  }

  private AngularUiRouterGraphBuilder.GraphNodesBuilder findDataObject(final AngularUiRouterDiagramModel model) {
    final Collection<AngularUiRouterNode> nodes = model.getNodes();
    for (AngularUiRouterNode node : nodes) {
      if (Type.topLevelTemplate.equals(node.getIdentifyingElement().getType())) {
        final VirtualFile rootFile = node.getIdentifyingElement().getNavigationTarget().getContainingFile().getVirtualFile();
        return myData.get(rootFile);
      }
    }
    return null;
  }
}
