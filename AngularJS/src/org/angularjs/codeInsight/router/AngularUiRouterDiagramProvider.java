package org.angularjs.codeInsight.router;

import com.intellij.diagram.*;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.diagram.presentation.DiagramState;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.graph.layout.Layouter;
import com.intellij.openapi.graph.layout.organic.SmartOrganicLayouter;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.ui.LightColors;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ArrayUtil;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class AngularUiRouterDiagramProvider extends BaseDiagramProvider<DiagramObject> {
  public static final String ANGULAR_UI_ROUTER = "Angular-ui-router";
  private DiagramVfsResolver<DiagramObject> myResolver;
  private AbstractDiagramElementManager<DiagramObject> myElementManager;
  private DiagramColorManagerBase myColorManager;

  private final Map<VirtualFile, AngularUiRouterGraphBuilder.GraphNodesBuilder> myData;

  public AngularUiRouterDiagramProvider() {
    myData = new HashMap<>();
    myResolver = new DiagramVfsResolver<DiagramObject>() {
      @Override
      public String getQualifiedName(DiagramObject element) {
        return (Type.template.equals(element.getType()) || Type.topLevelTemplate.equals(element.getType())) && element.getNavigationTarget() != null ?
                                                             element.getNavigationTarget().getContainingFile().getVirtualFile().getPath() : "";
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
        return ArrayUtil.toObjectArray(parent.getChildren().values());
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

  @Override
  public DiagramNodeContentManager getNodeContentManager() {
    return new DiagramNodeContentManager() {
      private final DiagramCategory[] CATEGORIES =
        new DiagramCategory[]{Type.Categories.STATE, Type.Categories.VIEW, Type.Categories.TEMPLATE, Type.Categories.TEMPLATE_PLACEHOLDER,
        Type.Categories.TOP_LEVEL_TEMPLATE};

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
    if (element == null || element.getNavigationTarget() == null) return null;
    final VirtualFile virtualFile = element.getNavigationTarget().getContainingFile().getVirtualFile();
    final AngularUiRouterGraphBuilder.GraphNodesBuilder nodesBuilder = myData.get(virtualFile);
    if (nodesBuilder == null) return null;
    return new AngularUiRouterDiagramModel(project, this, nodesBuilder.getAllNodes(), nodesBuilder.getEdges());
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
        final SmartOrganicLayouter layouter = GraphSettingsProvider.getInstance(project).getSettings(graph).getOrganicLayouter();
        layouter.setNodeEdgeOverlapAvoided(true);
        layouter.setParallelEdgeLayouterEnabled(true);
        return layouter;
      }
    };
  }
}
