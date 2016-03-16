package org.angularjs.codeInsight.router;

import com.intellij.diagram.*;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.diagram.presentation.DiagramState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Graph;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.graph.builder.actions.ActualZoomAction;
import com.intellij.openapi.graph.builder.actions.FitContentAction;
import com.intellij.openapi.graph.builder.actions.ZoomInAction;
import com.intellij.openapi.graph.builder.actions.ZoomOutAction;
import com.intellij.openapi.graph.builder.components.BasicGraphPresentationModel;
import com.intellij.openapi.graph.builder.renderer.AbstractColoredNodeCellRenderer;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.layout.organic.SmartOrganicLayouter;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.DefaultBackgroundRenderer;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.ui.LightColors;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.SmartList;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

/**
 * @author Irina.Chernushina on 3/9/2016.
 */
public class AngularUiRouterGraphBuilder {
  @NotNull private final Project myProject;
  private final Map<String, UiRouterState> myStatesMap;
  private final Map<String, Template> myTemplatesMap;
  private Graph2D myGraph;

  public AngularUiRouterGraphBuilder(@NotNull Project project,
                                     @NotNull Map<String, UiRouterState> statesMap,
                                     @NotNull Map<String, Template> templatesMap) {
    myProject = project;
    myStatesMap = statesMap;
    myTemplatesMap = templatesMap;
  }

  public JComponent build(@NotNull final Disposable disposable) {
    final MyDiagramProvider provider = new MyDiagramProvider();

    final GraphNodesBuilder nodesBuilder = new GraphNodesBuilder(myStatesMap, myTemplatesMap);
    nodesBuilder.build(provider);
    // todo create links between parent-child states by name parts AND parent attribute as separate stage
    // todo fill possible views as separate stage

    final GraphManager graphManager = GraphManager.getGraphManager();
    myGraph = graphManager.createGraph2D();
    final Graph2DView view = graphManager.createGraph2DView(myGraph);

    adjustLayout();

    final DefaultBackgroundRenderer backgroundRenderer = graphManager.createDefaultBackgroundRenderer(view);
    backgroundRenderer.setColor(UIUtil.getListBackground());
    view.setBackgroundRenderer(backgroundRenderer);

    final PresentationModel presentationModel = new PresentationModel(myProject, myGraph);
    final MyDiagramModel model = new MyDiagramModel(myProject, provider, nodesBuilder.getAllNodes(), nodesBuilder.getEdges());

    final GraphBuilder<MyNode, MyEdge> builder =
      GraphBuilderFactory.getInstance(myProject).createGraphBuilder(myGraph, view, model, presentationModel);
    presentationModel.setGraphBuilder(builder);

    GraphViewUtil.addDataProvider(view, new MyDataProvider(builder));
    Disposer.register(disposable, builder);

    builder.initialize();

    final Graph2DView builderView = builder.getView();
    //todo scroll bar bug!
    builderView.setScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    builderView.getJComponent().addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent e) {
        builderView.fitContent();
        builderView.adjustScrollBarVisibility();
        builderView.adjustScrollBarVisibility();
        builderView.getJComponent().removeComponentListener(this);
      }
    });

    return builderView.getJComponent();
  }

  private void adjustLayout() {
    final GraphSettings settings = GraphSettingsProvider.getInstance(myProject).getSettings(myGraph);
    //final CircularLayouter circularLayouter = settings.getCircularLayouter();
    //circularLayouter.setPlaceChildrenOnCommonRadiusEnabled(true);
    //circularLayouter.setLayoutStyle(CircularLayouter.SINGLE_CYCLE);
    //circularLayouter.setPartitionLayoutStyle(CircularLayouter.PARTITION_LAYOUTSTYLE_CYCLIC);

    final SmartOrganicLayouter organicLayouter = settings.getOrganicLayouter();
    settings.setCurrentLayouter(organicLayouter);
    /*myGraph.addDataProvider(CircularLayouter.CIRCULAR_CUSTOM_GROUPS_DPKEY, new com.intellij.openapi.graph.base.DataProvider() {
      @Override
      public Object get(Object _dataHolder) {
        if (_dataHolder instanceof Node) {
          final MyNode node = nodesBuilder.getAllNodes().get(((Node)_dataHolder).index());
          if (node != null) return node.getIdentifyingElement().getType();
        }
        return null;
      }

      @Override
      public int getInt(Object _dataHolder) {
        return 0;
      }

      @Override
      public double getDouble(Object _dataHolder) {
        return 0;
      }

      @Override
      public boolean getBool(Object _dataHolder) {
        return false;
      }
    });
    myGraph.addDataProvider(CircularLayouter.CIRCLE_ID_HOLDER_DPKEY, new com.intellij.openapi.graph.base.DataProvider() {
      @Override
      public Object get(Object _dataHolder) {
        return null;
      }

      @Override
      public boolean getBool(Object _dataHolder) {
        return false;
      }

      @Override
      public double getDouble(Object _dataHolder) {
        return 0;
      }

      @Override
      public int getInt(Object _dataHolder) {
        if (_dataHolder instanceof Node) {
          final MyNode node = nodesBuilder.getAllNodes().get(((Node)_dataHolder).index());
          if (node != null) return node.getIdentifyingElement().getType().ordinal();
        }
        return 0;
      }
    });*/
  }

  public DefaultActionGroup buildActions() {
    final DefaultActionGroup group = new DefaultActionGroup();

    group.add(new ZoomInAction(myGraph));
    group.add(new ZoomOutAction(myGraph));
    group.add(new ActualZoomAction(myGraph));
    group.add(new FitContentAction(myGraph));
    group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));

    return group;
  }

  private static class GraphNodesBuilder {
    public static final String DEFAULT = "<default>";
    @NotNull private final Map<String, UiRouterState> myStatesMap;
    @NotNull private final Map<String, Template> myTemplatesMap;

    private final Map<String, MyNode> stateNodes = new HashMap<>();
    private final Map<String, MyNode> templateNodes = new HashMap<>();
    private final Map<Pair<String, String>, MyNode> templatePlaceHoldersNodes = new HashMap<>();
    private final Map<Pair<String, String>, MyNode> viewNodes = new HashMap<>();
    private final List<MyEdge> edges = new ArrayList<>();

    private final List<MyNode> allNodes = new ArrayList<>();

    public GraphNodesBuilder(@NotNull Map<String, UiRouterState> statesMap,
                             @NotNull Map<String, Template> templatesMap) {
      myStatesMap = statesMap;
      myTemplatesMap = templatesMap;
    }

    public void build(@NotNull final MyDiagramProvider provider) {
      for (Map.Entry<String, UiRouterState> entry : myStatesMap.entrySet()) {
        final UiRouterState state = entry.getValue();
        final DiagramObject stateObject = new DiagramObject(Type.state, state.getName(), state.getPointer());
        if (state.getPointer() == null) {
          stateObject.addError("Can not find the state definition");
        }
        final MyNode node = new MyNode(stateObject, provider);
        stateNodes.put(state.getName(), node);
        final String templateUrl = normalizeTemplateUrl(state.getTemplateUrl());

        if (templateUrl != null) {
          final MyNode templateNode = getOrCreateTemplateNode(provider, templateUrl);
          edges.add(new MyEdge(node, templateNode));

          if (state.hasViews()) {
            if (state.isAbstract()) {
              stateObject.addWarning("Abstract state can not be instantiated so it makes no sense to define views for it.");
            }
            else {
              stateObject.addWarning("Since 'views' are defined for state, template information would be ignored.");
            }
          }
        }

        final List<UiView> views = state.getViews();
        if (views != null) {
          for (UiView view : views) {
            final String name = StringUtil.isEmptyOrSpaces(view.getName()) ? DEFAULT : view.getName();
            final DiagramObject viewObject = new DiagramObject(Type.view, name, view.getPointer());
            final MyNode viewNode = new MyNode(viewObject, provider);
            viewNodes.put(Pair.create(state.getName(), name), viewNode);

            final String template = view.getTemplate();
            if (!StringUtil.isEmptyOrSpaces(template)) {
              final MyNode templateNode = getOrCreateTemplateNode(provider, template);
              edges.add(new MyEdge(viewNode, templateNode, "provides"));
            }
            edges.add(new MyEdge(node, viewNode));
          }
        }
      }

      for (Map.Entry<String, MyNode> entry : stateNodes.entrySet()) {
        final String key = entry.getKey();
        final int dotIdx = key.lastIndexOf('.');
        if (dotIdx > 0) {
          final String parentKey = key.substring(0, dotIdx);
          MyNode parentState = stateNodes.get(parentKey);
          if (parentState != null) {
            edges.add(new MyEdge(entry.getValue(), parentState, "parent"));
          } else {
            final UiRouterState state = myStatesMap.get(key);
            if (state != null && state.getParentName() != null) {
              parentState = stateNodes.get(state.getParentName());
              edges.add(new MyEdge(entry.getValue(), parentState, "parent"));
            }
          }
        }
      }

      allNodes.addAll(stateNodes.values());
      allNodes.addAll(templateNodes.values());
      allNodes.addAll(templatePlaceHoldersNodes.values());
      allNodes.addAll(viewNodes.values());
    }

    public List<MyEdge> getEdges() {
      return edges;
    }

    public List<MyNode> getAllNodes() {
      return allNodes;
    }

    // todo use a separate step to calculate placeholders where the views are built in
    /*private String getParentTemplate(@NotNull final String view) {
      final int idx = view.indexOf("@");
      if (idx < 0) {
        // top level template
      }
    }*/

    @NotNull
    private MyNode getOrCreateTemplateNode(MyDiagramProvider provider, @NotNull String templateUrl) {
      final Template template = myTemplatesMap.get(templateUrl);
      if (template == null) {
        // file not found
        final DiagramObject templateObject = new DiagramObject(Type.template, templateUrl, null);
        templateObject.addError("Can not find template file");
        templateNodes.put(templateUrl, new MyNode(templateObject, provider));
      }
      else if (!templateNodes.containsKey(templateUrl)) {
        final DiagramObject templateObject = new DiagramObject(Type.template, templateUrl, template.getPointer());
        final MyNode templateNode = new MyNode(templateObject, provider);
        templateNodes.put(templateUrl, templateNode);

        final Map<String, SmartPsiElementPointer<PsiElement>> placeholders = template.getViewPlaceholders();
        if (placeholders != null) {
          for (Map.Entry<String, SmartPsiElementPointer<PsiElement>> pointerEntry : placeholders.entrySet()) {
            final String placeholder = pointerEntry.getKey();
            final DiagramObject placeholderObject = new DiagramObject(Type.templatePlaceholder,
                                                                      StringUtil.isEmptyOrSpaces(placeholder) ? DEFAULT : placeholder,
                                                                      pointerEntry.getValue());
            final MyNode placeholderNode = new MyNode(placeholderObject, provider);
            templatePlaceHoldersNodes.put(Pair.create(templateUrl, placeholder), placeholderNode);
            edges.add(new MyEdge(templateNode, placeholderNode));
          }
        }
      }
      final MyNode templateNode = templateNodes.get(templateUrl);
      assert templateNode != null;
      return templateNode;
    }


  }

  public static String normalizeTemplateUrl(@Nullable String url) {
    if (url == null) return null;
    url = url.startsWith("/") ? url.substring(1) : url;
    url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    return url;
  }

  private static class MyDiagramProvider extends BaseDiagramProvider<DiagramObject> {
    private DiagramVfsResolver<DiagramObject> myResolver;
    private AbstractDiagramElementManager<DiagramObject> myElementManager;

    public MyDiagramProvider() {
      myResolver = new DiagramVfsResolver<DiagramObject>() {
        @Override
        public String getQualifiedName(DiagramObject element) {
          return "";
        }

        @Nullable
        @Override
        public DiagramObject resolveElementByFQN(String fqn, Project project) {
          return null;
        }
      };
      myElementManager = new AbstractDiagramElementManager<DiagramObject>() {
        @Nullable
        @Override
        public DiagramObject findInDataContext(DataContext context) {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAcceptableAsNode(Object element) {
          throw new UnsupportedOperationException();
        }

        @Nullable
        @Override
        public String getElementTitle(DiagramObject element) {
          throw new UnsupportedOperationException();
        }

        @Nullable
        @Override
        public SimpleColoredText getItemName(Object element, DiagramState presentation) {
          throw new UnsupportedOperationException();
        }

        @Override
        public String getNodeTooltip(DiagramObject element) {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public String getID() {
      return "Angular-ui-router";
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

    @Override
    public DiagramNodeContentManager getNodeContentManager() {
      return new DiagramNodeContentManager() {
        private final DiagramCategory[] CATEGORIES = new DiagramCategory[]{STATE, VIEW, TEMPLATE};

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
    public DiagramDataModel<DiagramObject> createDataModel(@NotNull Project project,
                                                           @Nullable DiagramObject element,
                                                           @Nullable VirtualFile file,
                                                           DiagramPresentationModel presentationModel) {
      return null;
    }
  }

  private final static DiagramCategory STATE = new DiagramCategory("States", AllIcons.Hierarchy.Class, true);
  private final static DiagramCategory VIEW = new DiagramCategory("Views", AllIcons.Hierarchy.Base, true);
  private final static DiagramCategory TEMPLATE = new DiagramCategory("Templates", AllIcons.Actions.EditSource, true);
  private final static DiagramCategory TEMPLATE_PLACEHOLDER = new DiagramCategory("TemplatePlaceholders", AllIcons.Actions.Unselectall, true);

  private enum Type {
    state(STATE),
    view(VIEW),
    template(TEMPLATE),
    templatePlaceholder(TEMPLATE_PLACEHOLDER);

    private final DiagramCategory myCategory;

    Type(DiagramCategory category) {
      myCategory = category;
    }

    public DiagramCategory getCategory() {
      return myCategory;
    }
  }

  private static class DiagramObject {
    @NotNull private final Type myType;
    @NotNull private final String myName;
    @Nullable private final SmartPsiElementPointer myNavigationTarget;
    private boolean myIsValid = true;  //invalid = created by reference from other place, but not defined
    @NotNull private final List<String> myWarnings;
    @NotNull private final List<String> myErrors;

    public DiagramObject(@NotNull Type type, @NotNull String name, @Nullable SmartPsiElementPointer navigationTarget) {
      myType = type;
      myName = name;
      myNavigationTarget = navigationTarget;
      myWarnings = new SmartList<>();
      myErrors = new SmartList<>();
    }

    @NotNull
    public Type getType() {
      return myType;
    }

    @NotNull
    public String getName() {
      return myName;
    }

    @Nullable
    public SmartPsiElementPointer getNavigationTarget() {
      return myNavigationTarget;
    }

    public void addError(@NotNull final String error) {
      myErrors.add(error);
      myIsValid = false;
    }

    public void addWarning(@NotNull final String warning) {
      myWarnings.add(warning);
      myIsValid = false;
    }

    public boolean isValid() {
      return myIsValid;
    }

    @NotNull
    public List<String> getErrors() {
      return myErrors;
    }

    @NotNull
    public List<String> getWarnings() {
      return myWarnings;
    }
  }

  private static class MyNode extends DiagramNodeBase<DiagramObject> {
    @NotNull private final DiagramObject myDiagramObject;

    public MyNode(@NotNull DiagramObject diagramObject, @NotNull DiagramProvider<DiagramObject> provider) {
      super(provider);
      myDiagramObject = diagramObject;
    }

    @Nullable
    @Override
    public String getTooltip() {
      return myDiagramObject.getName();
    }

    @Override
    public Icon getIcon() {
      return null;
    }

    @NotNull
    @Override
    public DiagramObject getIdentifyingElement() {
      return myDiagramObject;
    }
  }

  private final static DiagramRelationshipInfo DEPENDS = new DiagramRelationshipInfoAdapter("DEPENDS", DiagramLineType.SOLID) {
    @Override
    public Shape getStartArrow() {
      return NONE;
    }

    @Override
    public Shape getEndArrow() {
      return STANDARD;
    }
  };

  private static class MyEdge extends DiagramEdgeBase<DiagramObject> {
    public MyEdge(DiagramNode<DiagramObject> source, DiagramNode<DiagramObject> target) {
      super(source, target, DEPENDS);
    }

    public MyEdge(DiagramNode<DiagramObject> source, DiagramNode<DiagramObject> target, final String label) {
      super(source, target, new DiagramRelationshipInfoAdapter("BUILTIN", DiagramLineType.SOLID) {
        @Override
        public String getLabel() {
          return label;
        }

        @Override
        public Shape getStartArrow() {
          return NONE;
        }

        @Override
        public Shape getEndArrow() {
          return STANDARD;
        }
      });
    }
  }

  private static class PresentationModel extends BasicGraphPresentationModel<MyNode, MyEdge> {
    private final Project myProject;
    private MyNodeRenderer myNodeCellRenderer;

    public PresentationModel(Project project, Graph graph) {
      super(graph);
      myProject = project;
    }

    @Override
    public String getNodeTooltip(@Nullable MyNode node) {
      return node == null ? "" : node.getTooltip();
    }

    @Override
    public String getEdgeTooltip(@Nullable MyEdge edge) {
      return edge == null ? "" : edge.getName();
    }

    @NotNull
    @Override
    public NodeRealizer getNodeRealizer(@Nullable MyNode node) {
      if (myNodeCellRenderer == null) {
        myNodeCellRenderer = new MyNodeRenderer(getGraphBuilder(), PsiManager.getInstance(myProject).getModificationTracker());
      }
      return GraphViewUtil.createNodeRealizer("AngularUiRouterRenderer", myNodeCellRenderer);
    }

    private class MyNodeRenderer extends AbstractColoredNodeCellRenderer {
      @NotNull private final GraphBuilder<MyNode, MyEdge> myBuilder;

      protected MyNodeRenderer(@NotNull GraphBuilder<MyNode, MyEdge> builder, ModificationTracker modificationTracker) {
        super(modificationTracker);
        myBuilder = builder;
      }

      @Override
      public void tuneNode(NodeRealizer realizer, JPanel wrapper) {
        wrapper.removeAll();

        final Node node = realizer.getNode();
        final MyNode nodeObject = myBuilder.getNodeObject(node);
        if (nodeObject != null) {
          wrapper.add(new JBLabel(nodeObject.getTooltip()), BorderLayout.CENTER);
          final DiagramObject element = nodeObject.getIdentifyingElement();
          if (Type.state.equals(element.getType())) {
            wrapper.setBackground(LightColors.YELLOW);
          } else if (Type.view.equals(element.getType())) {
            wrapper.setBackground(LightColors.BLUE);
          } else if (Type.template.equals(element.getType())) {
            wrapper.setBackground(LightColors.GREEN);
          } else if (Type.templatePlaceholder.equals(element.getType())) {
            wrapper.setBackground(LightColors.SLIGHTLY_GREEN);
          }
        }
      }
    }
  }

  private static class MyDiagramModel extends GraphDataModel<MyNode, MyEdge> {
    @NotNull private final List<MyNode> myNodes;
    @NotNull private final List<MyEdge> myEdges;

    public MyDiagramModel(Project project, DiagramProvider<DiagramObject> provider, @NotNull final List<MyNode> nodes,
                          @NotNull final List<MyEdge> edges) {
      myNodes = nodes;
      myEdges = edges;
    }

    @NotNull
    @Override
    public Collection<MyNode> getNodes() {
      return myNodes;
    }

    @NotNull
    @Override
    public Collection<MyEdge> getEdges() {
      return myEdges;
    }

    @NotNull
    @Override
    public MyNode getSourceNode(MyEdge edge) {
      return (MyNode)edge.getSource();
    }

    @NotNull
    @Override
    public MyNode getTargetNode(MyEdge edge) {
      return (MyNode)edge.getTarget();
    }

    @NotNull
    @Override
    public String getNodeName(MyNode node) {
      return node.getTooltip();
    }

    @NotNull
    @Override
    public String getEdgeName(MyEdge edge) {
      return edge.getName();
    }

    @Nullable
    @Override
    public MyEdge createEdge(@NotNull MyNode from,
                             @NotNull MyNode to) {
      return null;
    }

    @Override
    public void dispose() {

    }
  }

  private static class MyDataProvider implements DataProvider {
    private final Project myProject;
    private final Graph2D myGraph;
    private final GraphBuilder<MyNode, MyEdge> myBuilder;

    public MyDataProvider(GraphBuilder<MyNode, MyEdge> builder) {
      myBuilder = builder;
      myProject = builder.getProject();
      myGraph = builder.getGraph();
    }

    @Nullable
    @Override
    public Object getData(@NonNls String dataId) {
      if (CommonDataKeys.PROJECT.is(dataId)) {
        return myProject;
      }
      else if (CommonDataKeys.PSI_ELEMENT.is(dataId)) {
        for (Node node : myGraph.getNodeArray()) {
          if (myGraph.getRealizer(node).isSelected()) {
            final MyNode object = myBuilder.getNodeObject(node);
            if (object != null) {
              final SmartPsiElementPointer pointer = object.getIdentifyingElement().getNavigationTarget();
              return pointer == null ? null : pointer.getElement();
            }
          }
        }
      }
      return null;
    }
  }
}
