package com.jetbrains.profiler;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.actionscript.profiler.base.ProfilerActionGroup;
import org.jetbrains.annotations.NotNull;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class ProfileView extends UserDataHolderBase implements FileEditor, ProfilerActionGroup {
  private PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport(this);
  private final VirtualFile myFile;
  private final Project myProject;

  public ProfileView(VirtualFile file, Project project) {
    myFile = file;
    myProject = project;

    project.getMessageBus().connect().subscribe(UISettingsListener.TOPIC, uiSettings -> uiSettingsChange());
  }

  protected void uiSettingsChange() {
  }

  public Project getProject() {
    return myProject;
  }

  @Override
  @NotNull
  public String getName() {
    return myFile.getNameWithoutExtension();
  }

  @Override
  public void setState(@NotNull FileEditorState fileEditorState) {
    // TODO:
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void selectNotify() {
  }

  @Override
  public void deselectNotify() {
  }

  @Override
  public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
    myPropertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
  }

  @Override
  public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
    myPropertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
  }

  @Override
  public BackgroundEditorHighlighter getBackgroundHighlighter() {
    return null;
  }

  @Override
  public FileEditorLocation getCurrentLocation() {
    return null;
  }

  @Override
  public StructureViewBuilder getStructureViewBuilder() {
    return null;
  }

  @Override
  public void dispose() {
    myPropertyChangeSupport = null;
  }
}
