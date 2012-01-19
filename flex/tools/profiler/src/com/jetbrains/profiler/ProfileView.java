package com.jetbrains.profiler;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.actionscript.profiler.base.ProfilerActionGroup;
import org.jetbrains.annotations.NotNull;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
* Created by IntelliJ IDEA.
* User: Maxim
* Date: 02.09.2010
* Time: 13:42:28
* To change this template use File | Settings | File Templates.
*/
public abstract class ProfileView extends UserDataHolderBase implements FileEditor, ProfilerActionGroup {
  private PropertyChangeSupport myPropertyChangeSupport = new PropertyChangeSupport(this);
  private final VirtualFile myFile;
  private final Project myProject;

  public ProfileView(VirtualFile file, Project project) {
    myFile = file;
    myProject = project;

    UISettings.getInstance().addUISettingsListener(new UISettingsListener() {
      @Override
      public void uiSettingsChanged(UISettings source) {
        uiSettingsChange();
      }
    }, myProject);
  }

  protected void uiSettingsChange() {
  }

  public Project getProject() {
    return myProject;
  }

  @NotNull
  public String getName() {
    return myFile.getNameWithoutExtension();
  }

  @NotNull
  public FileEditorState getState(@NotNull FileEditorStateLevel fileEditorStateLevel) {
    // TODO:
    return new FileEditorState() {
      public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return false;
      }
    };
  }

  public void setState(@NotNull FileEditorState fileEditorState) {
    // TODO:
  }

  public boolean isModified() {
    return false;
  }

  public boolean isValid() {
    return true;
  }

  public void selectNotify() {
  }

  public void deselectNotify() {
  }

  public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
    myPropertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
  }

  public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
    myPropertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
  }

  public BackgroundEditorHighlighter getBackgroundHighlighter() {
    return null;
  }

  public FileEditorLocation getCurrentLocation() {
    return null;
  }

  public StructureViewBuilder getStructureViewBuilder() {
    return null;
  }

  public void dispose() {
    myPropertyChangeSupport = null;
  }
}
