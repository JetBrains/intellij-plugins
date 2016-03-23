package org.angularjs.codeInsight.router;

import com.intellij.diagram.*;
import com.intellij.diagram.presentation.DiagramState;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.graph.base.Edge;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;

/**
 * @author Irina.Chernushina on 3/23/2016.
 */
public class DiagramBuilderWrapper implements DiagramBuilder {
  private final Project myProject;
  private final DiagramProvider myDiagramProvider;
  private final DiagramDataModel myDataModel;
  private final DiagramPresentationModel myPresentationModel;

  public DiagramBuilderWrapper(Project project,
                               DiagramProvider provider,
                               DiagramDataModel model,
                               DiagramPresentationModel presentationModel) {
    myProject = project;
    myDiagramProvider = provider;
    myDataModel = model;
    myPresentationModel = presentationModel;
  }

  @Override
  public Project getProject() {
    return myProject;
  }

  @Override
  public Graph2D getGraph() {
    return null;
  }

  @Override
  public Graph2DView getView() {
    return null;
  }

  @Override
  public Collection<DiagramNode> getNodeObjects() {
    return null;
  }

  @Override
  public Node getNode(DiagramNode node) {
    return null;
  }

  @Override
  public DiagramNode getNodeObject(Node node) {
    return null;
  }

  @Override
  public Collection<DiagramEdge> getEdgeObjects() {
    return null;
  }

  @Override
  public Edge getEdge(DiagramEdge edge) {
    return null;
  }

  @Override
  public DiagramEdge getEdgeObject(Edge edge) {
    return null;
  }

  @Override
  public void setAllowEdgeCreation(boolean allow) {

  }

  @Override
  public DiagramState getPresentation() {
    return null;
  }

  @Override
  public DiagramPresentationModel getPresentationModel() {
    return myPresentationModel;
  }

  @Override
  public DiagramDataModel getDataModel() {
    return myDataModel;
  }

  @Override
  public DiagramProvider getProvider() {
    return myDiagramProvider;
  }

  @Override
  public void setSelected(@NotNull DiagramNode<?> node, boolean selected) {

  }

  @Override
  public void setSelected(@NotNull DiagramEdge<?> edge, boolean selected) {

  }

  @Nullable
  @Override
  public DiagramFileEditor getEditor() {
    return null;
  }

  @Override
  public void setEditor(DiagramFileEditor editor) {

  }

  @Nullable
  @Override
  public DocumentReference getDocumentReference() {
    return null;
  }

  @Override
  public void update() {

  }

  @Override
  public void update(boolean increaseModTrackerCounter, boolean updateLayout) {

  }

  @Override
  public boolean isPopupMode() {
    return false;
  }

  @Nullable
  @Override
  public JBPopup getPopup() {
    return null;
  }

  @Override
  public void setPopup(JBPopup popup) {

  }

  @Override
  public void relayout() {

  }

  @Override
  public void updateGraph() {

  }

  @Override
  public void updateDataModel() {

  }

  @Override
  public void initialize() {

  }

  @Override
  public void updateView() {

  }

  @Override
  public void createDraggedNode(DiagramNode node, String nodeName, Point point) {

  }

  @Override
  public void requestFocus() {

  }

  @Override
  public double zoomView(double scale) {
    return 0;
  }

  @Override
  public void notifyOtherBuilders() {

  }

  @Override
  public void dispose() {

  }

  @Nullable
  @Override
  public <T> T getUserData(@NotNull Key<T> key) {
    return null;
  }

  @Override
  public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

  }
}
