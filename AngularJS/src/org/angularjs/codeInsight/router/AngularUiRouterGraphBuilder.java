// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.router;

import com.intellij.diagram.DiagramProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.angularjs.AngularJSBundle.message;
import static org.angularjs.codeInsight.router.Type.state;
import static org.angularjs.codeInsight.router.Type.template;

public final class AngularUiRouterGraphBuilder {
  private final @NotNull Project myProject;
  private final Map<String, UiRouterState> myStatesMap;
  private final Map<VirtualFile, Template> myTemplatesMap;
  private final @Nullable RootTemplate myRootTemplate;
  private final VirtualFile myKey;

  public AngularUiRouterGraphBuilder(@NotNull Project project,
                                     @NotNull Map<String, UiRouterState> statesMap,
                                     @NotNull Map<VirtualFile, Template> templatesMap,
                                     @Nullable RootTemplate rootTemplate, VirtualFile key) {
    myProject = project;
    myStatesMap = statesMap;
    myTemplatesMap = templatesMap;
    myRootTemplate = rootTemplate;
    myKey = key;
  }

  public VirtualFile getKey() {
    return myKey;
  }

  public GraphNodesBuilder createDataModel(@NotNull DiagramProvider<DiagramObject> provider) {
    final GraphNodesBuilder nodesBuilder = new GraphNodesBuilder(myStatesMap, myTemplatesMap, myRootTemplate, myKey);
    nodesBuilder.build(provider, myProject);

    return nodesBuilder;
  }

  public static class GraphNodesBuilder {
    public static final String DEFAULT = "$default";
    private final @NotNull Map<String, UiRouterState> myStatesMap;
    private final @NotNull Map<VirtualFile, Template> myTemplatesMap;
    private final @Nullable RootTemplate myRootTemplate;
    private final VirtualFile myKey;

    private AngularUiRouterNode myRootNode;
    private final Map<String, AngularUiRouterNode> stateNodes = new HashMap<>();
    private final Map<String, AngularUiRouterNode> templateNodes = new HashMap<>();
    private final Map<Pair<String, String>, DiagramObject> templatePlaceHoldersNodes = new HashMap<>();
    private final Map<Pair<String, String>, DiagramObject> viewNodes = new HashMap<>();
    private final List<AngularUiRouterEdge> edges = new ArrayList<>();

    private final List<AngularUiRouterNode> allNodes = new ArrayList<>();

    public GraphNodesBuilder(@NotNull Map<String, UiRouterState> statesMap,
                             @NotNull Map<VirtualFile, Template> templatesMap, @Nullable RootTemplate rootTemplate, VirtualFile key) {
      myStatesMap = statesMap;
      myTemplatesMap = templatesMap;
      myRootTemplate = rootTemplate;
      myKey = key;
    }

    public AngularUiRouterNode getRootNode() {
      return myRootNode;
    }

    public VirtualFile getKey() {
      return myKey;
    }

    public void build(final @NotNull DiagramProvider<DiagramObject> provider, final @NotNull Project project) {
      final DiagramObject rootDiagramObject;
      if (myRootTemplate != null) {
        myRootNode = getOrCreateTemplateNode(provider, myKey, normalizeTemplateUrl(myRootTemplate.getRelativeUrl()),
                                             myRootTemplate.getTemplate());
        myRootNode.getIdentifyingElement().setType(Type.topLevelTemplate);
      }
      else {
        // todo remove from diagram if not used
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(myKey);
        rootDiagramObject = new DiagramObject(
          Type.topLevelTemplate, myKey.getName(),
          psiFile == null ? null : SmartPointerManager.getInstance(project).createSmartPsiElementPointer(psiFile));
        myRootNode = new AngularUiRouterNode(rootDiagramObject, provider);
      }

      setParentStates();
      for (Map.Entry<String, UiRouterState> entry : myStatesMap.entrySet()) {
        final UiRouterState state = entry.getValue();
        final DiagramObject stateObject = new DiagramObject(Type.state, state.getName(), state.getPointer());
        if (state.getParentName() != null) {
          stateObject.setParent(state.getParentName());
        }
        if (state.getPointer() == null) {
          stateObject.addError(message("angularjs.ui.router.diagram.error.cant.find.state.definition"));
        }
        final AngularUiRouterNode node = new AngularUiRouterNode(stateObject, provider);
        stateNodes.put(state.getName(), node);
        final String templateUrl = normalizeTemplateUrl(state.getTemplateUrl());

        if (templateUrl != null && !state.hasViews()) {
          final AngularUiRouterNode templateNode = getOrCreateTemplateNode(provider, state.getTemplateFile(), templateUrl, null);
          edges.add(new AngularUiRouterEdge(templateNode, node, message("angularjs.ui.router.diagram.edge.provides"),
                                            AngularUiRouterEdge.Type.providesTemplate));
        }
        else if (state.isHasTemplateDefined() && state.getTemplatePointer() != null) {
          final PsiElement element = state.getTemplatePointer().getElement();
          if (element != null && element.isValid()) {
            final AngularUiRouterNode localTemplateNode = createLocalTemplate(element, provider);
            edges.add(new AngularUiRouterEdge(localTemplateNode, node, message("angularjs.ui.router.diagram.edge.provides"),
                                              AngularUiRouterEdge.Type.providesTemplate));
          }
        }
        if (state.hasViews()) {
          if (state.isAbstract()) {
            stateObject.addWarning(message("angularjs.ui.router.diagram.warning.abstract.state"));
          }
          else if (templateUrl != null || state.isHasTemplateDefined()) {
            stateObject.addWarning(message("angularjs.ui.router.diagram.warning.views.are.defined"));
          }
        }
        if (state.isHasTemplateDefined() && state.getTemplatePointer() == null) {
          stateObject.addNote(message("angularjs.ui.router.diagram.note.has.embedded.template.definition"));
        }
      }

      for (Map.Entry<String, UiRouterState> entry : myStatesMap.entrySet()) {
        final UiRouterState state = entry.getValue();
        final AngularUiRouterNode node = stateNodes.get(state.getName());
        assert node != null;

        final List<UiView> views = state.getViews();
        if (views != null && !views.isEmpty()) {
          for (UiView view : views) {
            final String name = StringUtil.isEmptyOrSpaces(view.getName()) ? DEFAULT : view.getName();
            final DiagramObject viewObject = new DiagramObject(Type.view, name, view.getPointer());
            viewNodes.put(Pair.create(state.getName(), name), viewObject);

            final String template = view.getTemplate();
            if (!StringUtil.isEmptyOrSpaces(template)) {
              final AngularUiRouterNode templateNode = getOrCreateTemplateNode(provider, view.getTemplateFile(), template, null);
              edges.add(new AngularUiRouterEdge(templateNode, node, message("angularjs.ui.router.diagram.edge.smth.provides", name),
                                                AngularUiRouterEdge.Type.providesTemplate).setTargetName(name));
            }
            else if (view.getTemplatePointer() != null) {
              final PsiElement element = view.getTemplatePointer().getElement();
              if (element != null && element.isValid()) {
                final AngularUiRouterNode localTemplateNode = createLocalTemplate(element, provider);
                edges.add(new AngularUiRouterEdge(localTemplateNode, node, message("angularjs.ui.router.diagram.edge.smth.provides", name),
                                                  AngularUiRouterEdge.Type.providesTemplate).setTargetName(name));
              }
            }
            node.getIdentifyingElement().addChild(viewObject, node);
          }
        }
      }

      // views can also refer to different states, so first all state nodes must be created
      for (Map.Entry<String, UiRouterState> entry : myStatesMap.entrySet()) {
        final UiRouterState state = entry.getValue();
        final AngularUiRouterNode node = stateNodes.get(state.getName());
        assert node != null;

        final List<UiView> views = state.getViews();
        if (views != null && !views.isEmpty()) {
          for (UiView view : views) {
            final String name = StringUtil.isEmptyOrSpaces(view.getName()) ? DEFAULT : view.getName();
            final DiagramObject viewNode = viewNodes.get(Pair.create(state.getName(), name));
            assert viewNode != null;

            final Pair<AngularUiRouterNode, String> pair = getParentTemplateNode(state.getName(), view.getName());
            if (pair != null && pair.getFirst() != null) {
              connectViewOrStateWithPlaceholder(node, name, pair);
            }
          }
        }
        else {
          //find unnamed parent template for view
          final Pair<AngularUiRouterNode, String> pair = getParentTemplateNode(state.getName(), "");
          if (pair != null && pair.getFirst() != null) {
            connectViewOrStateWithPlaceholder(node, DEFAULT, pair);
          }
        }
      }
      createStateParentEdges();

      final List<AngularUiRouterNode> list = new ArrayList<>();
      list.add(myRootNode);
      list.addAll(stateNodes.values());
      list.addAll(templateNodes.values());
      for (AngularUiRouterNode node : list) {
        if (!allNodes.contains(node)) allNodes.add(node);
      }
    }

    private AngularUiRouterNode createLocalTemplate(PsiElement element, DiagramProvider<DiagramObject> provider) {
      final String name = element.getContainingFile().getName() + " (" + message("angularjs.ui.router.diagram.node.name.local") + ")";
      final @NlsSafe String key = element.getContainingFile().getVirtualFile().getUrl() + ":" + element.getTextRange().getStartOffset();
      final Template template = AngularUiRouterDiagramBuilder.readTemplateFromFile(element.getProject(), name, element);
      if (!templateNodes.containsKey(key)) {
        final DiagramObject templateObject = new DiagramObject(Type.template, name, template.getPointer());
        final AngularUiRouterNode templateNode = new AngularUiRouterNode(templateObject, provider);
        templateNodes.put(key, templateNode);

        putPlaceholderNodes(key, template, templateNode);
      }
      final AngularUiRouterNode templateNode = templateNodes.get(key);
      assert templateNode != null;
      templateNode.getIdentifyingElement().setTooltip(key);
      return templateNode;
    }

    private void connectViewOrStateWithPlaceholder(AngularUiRouterNode stateNode, String viewName, Pair<AngularUiRouterNode, String> pair) {
      final String placeholderName = pair.getSecond();
      //final String placeholderName = StringUtil.isEmptyOrSpaces(pair.getSecond()) ? DEFAULT : pair.getSecond();
      String usedTemplateUrl = null;

      final Type nodeType = pair.getFirst().getIdentifyingElement().getType();
      if (template.equals(nodeType) || Type.topLevelTemplate.equals(nodeType)) {
        usedTemplateUrl = pair.getFirst().getIdentifyingElement().getTooltip();
      }
      else if (state.equals(nodeType)) {
        final String parentState = pair.getFirst().getIdentifyingElement().getName();
        final UiRouterState parentStateObject = myStatesMap.get(parentState);
        if (parentStateObject != null) {
          if (parentStateObject.hasViews()) {
            final List<UiView> parentViews = parentStateObject.getViews();
            for (UiView parentView : parentViews) {
              if (placeholderName.equals(parentView.getName())) {
                usedTemplateUrl = parentView.getTemplate();
                break;
              }
            }
          }
          else if (!StringUtil.isEmptyOrSpaces(parentStateObject.getTemplateUrl())) {
            usedTemplateUrl = parentStateObject.getTemplateUrl();
          }
        }
      }

      usedTemplateUrl = normalizeTemplateUrl(usedTemplateUrl);
      final DiagramObject placeholder = templatePlaceHoldersNodes.get(Pair.create(usedTemplateUrl, placeholderName));
      if (placeholder != null && placeholder.getContainer() != null) {
        final AngularUiRouterEdge edge = new AngularUiRouterEdge(
          placeholder.getContainer(), stateNode, message("angularjs.ui.router.diagram.edge.smth.populates.smth", viewName, placeholderName),
          AngularUiRouterEdge.Type.fillsTemplate
        ).setSourceName(placeholderName).setTargetName(viewName);
        edge.setTargetAnchor(placeholder);
        edges.add(edge);
      }
    }

    private void createStateParentEdges() {
      for (Map.Entry<String, AngularUiRouterNode> entry : stateNodes.entrySet()) {
        final String key = entry.getKey();
        final UiRouterState state = myStatesMap.get(key);
        if (state != null && state.getParentName() != null) {
          final AngularUiRouterNode parentState = stateNodes.get(state.getParentName());
          if (parentState != null) {
            edges.add(new AngularUiRouterEdge(parentState, entry.getValue(), "", AngularUiRouterEdge.Type.parent));
          }
        }
      }
    }

    private void setParentStates() {
      for (Map.Entry<String, UiRouterState> entry : myStatesMap.entrySet()) {
        if (!StringUtil.isEmptyOrSpaces(entry.getValue().getParentName())) continue;
        final String key = entry.getKey();
        final int dotIdx = key.lastIndexOf('.');
        if (dotIdx > 0) {
          final String parentKey = key.substring(0, dotIdx);
          entry.getValue().setParentName(parentKey);
        }
      }
    }

    public List<AngularUiRouterNode> getStateTemplates(final @NotNull AngularUiRouterNode state) {
      final List<AngularUiRouterNode> list = new ArrayList<>();
      if (!Type.state.equals(state.getIdentifyingElement().getType())) return Collections.emptyList();
      for (AngularUiRouterEdge edge : edges) {
        if (AngularUiRouterEdge.Type.providesTemplate.equals(edge.getType()) && edge.getTarget().equals(state) &&
            template.equals(edge.getSource().getIdentifyingElement().getType())) {
          list.add((AngularUiRouterNode)edge.getSource());
        }
      }
      return list;
    }

    public List<AngularUiRouterNode> getZeroLevelStates() {
      final List<AngularUiRouterNode> list = new ArrayList<>();
      for (AngularUiRouterNode current : allNodes) {
        if (state.equals(current.getIdentifyingElement().getType()) && current.getIdentifyingElement().getParent() == null) {
          list.add(current);
        }
      }
      return list;
    }

    public List<AngularUiRouterNode> getImmediateChildrenStates(@NotNull AngularUiRouterNode node) {
      if (myRootNode.equals(node)) return getZeroLevelStates();

      final String name = node.getIdentifyingElement().getName();
      final List<AngularUiRouterNode> list = new ArrayList<>();
      final DiagramObject diagramObject = node.getIdentifyingElement();
      if (!state.equals(diagramObject.getType())) return Collections.emptyList();
      for (AngularUiRouterNode current : allNodes) {
        if (state.equals(current.getIdentifyingElement().getType()) && name.equals(current.getIdentifyingElement().getParent())) {
          list.add(current);
        }
      }
      return list;
    }

    public List<AngularUiRouterEdge> getEdges() {
      return edges;
    }

    public List<AngularUiRouterNode> getAllNodes() {
      return allNodes;
    }

    private @Nullable Pair<AngularUiRouterNode, String> getParentTemplateNode(final @NotNull String state, final @NotNull String view) {
      final int idx = view.indexOf("@");
      if (idx < 0) {
        // parent or top level template
        if (state.contains(".") || myStatesMap.get(state).getParentName() != null) {
          final UiRouterState routerState = myStatesMap.get(state);
          if (routerState == null) {
            return null;
          }
          return Pair.create(stateNodes.get(routerState.getParentName()), view);
        }
        else {
          return Pair.create(myRootNode, view);
        }
      }
      else {
        //absolute path
        //if (idx == 0) return Pair.create(myRootNode, view.substring(1));
        final String placeholderName = view.substring(0, idx);
        final String stateName = view.substring(idx + 1);
        if (StringUtil.isEmptyOrSpaces(stateName)) {
          return Pair.create(myRootNode, placeholderName);
        }
        return Pair.create(stateNodes.get(stateName), placeholderName);
      }
    }

    private @NotNull AngularUiRouterNode getOrCreateTemplateNode(DiagramProvider<DiagramObject> provider,
                                                                 @Nullable VirtualFile templateFile,
                                                                 @NotNull String templateUrl, @Nullable Template template) {
      final @NlsSafe String fullUrl = templateUrl;
      final int idx = fullUrl.lastIndexOf('/');
      templateUrl = idx >= 0 ? templateUrl.substring(idx + 1) : templateUrl;
      template = template == null && templateFile != null ? myTemplatesMap.get(templateFile) : template;
      if (template == null || template.getPointer() == null || templateFile == null) {
        final AngularUiRouterNode templateNode = templateNodes.get(fullUrl);
        if (templateNode != null) return templateNode;

        // file not found
        final DiagramObject templateObject = new DiagramObject(Type.template, templateUrl, null);
        templateObject.addError(message("angularjs.ui.router.diagram.error.cant.find.template.file"));
        final AngularUiRouterNode fictiveNode = new AngularUiRouterNode(templateObject, provider);
        fictiveNode.getIdentifyingElement().setTooltip(fullUrl);
        templateNodes.put(fullUrl, fictiveNode);
        return fictiveNode;
      }
      else if (!templateNodes.containsKey(templateFile.getUrl())) {
        final DiagramObject templateObject = new DiagramObject(Type.template, templateUrl, template.getPointer());
        final AngularUiRouterNode templateNode = new AngularUiRouterNode(templateObject, provider);
        templateNodes.put(templateFile.getUrl(), templateNode);

        putPlaceholderNodes(fullUrl, template, templateNode);
      }
      final AngularUiRouterNode templateNode = templateNodes.get(templateFile.getUrl());
      assert templateNode != null;
      templateNode.getIdentifyingElement().setTooltip(fullUrl);
      return templateNode;
    }

    private void putPlaceholderNodes(@NotNull String templateUrl,
                                     Template template,
                                     AngularUiRouterNode templateNode) {
      final Map<String, SmartPsiElementPointer<PsiElement>> placeholders = template.getViewPlaceholders();
      if (placeholders != null) {
        for (Map.Entry<String, SmartPsiElementPointer<PsiElement>> pointerEntry : placeholders.entrySet()) {
          final String placeholder = pointerEntry.getKey();
          final DiagramObject placeholderObject = new DiagramObject(Type.templatePlaceholder,
                                                                    StringUtil.isEmptyOrSpaces(placeholder) ? DEFAULT : placeholder,
                                                                    pointerEntry.getValue());
          //final MyNode placeholderNode = new MyNode(placeholderObject, provider);
          templateNode.getIdentifyingElement().addChild(placeholderObject, templateNode);
          templatePlaceHoldersNodes.put(Pair.create(templateUrl, placeholder), placeholderObject);
          //          final MyEdge edge = new MyEdge(templateNode, placeholderNode);
          //          edges.add(edge);
        }
      }
    }
  }

  public static String normalizeTemplateUrl(@Nullable String url) {
    if (url == null) return null;
    url = url.startsWith("/") ? url.substring(1) : url;
    url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    return url;
  }
}
