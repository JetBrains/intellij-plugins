package com.jetbrains.profiler;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * User: Maxim
 * Date: 02.09.2010
 * Time: 13:35:23
 */
public class ProfileViewProvider implements FileEditorProvider, DumbAware {

  public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    for(ProfilerSnapshotProvider provider:ProfilerSnapshotProvider.ProfileSnapshotProvider_EP.getExtensions()) {
      if (provider.accepts(virtualFile)) return true;
    }
    return false;
  }

  @NotNull
  public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    for(ProfilerSnapshotProvider provider:ProfilerSnapshotProvider.ProfileSnapshotProvider_EP.getExtensions()) {
      if (provider.accepts(virtualFile)) return provider.createView(virtualFile, project);
    }
    assert false;
    return null;
  }

  public void disposeEditor(@NotNull FileEditor fileEditor) {
    Disposer.dispose(fileEditor);
  }

  @NotNull
  public FileEditorState readState(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile virtualFile) {
    return new FileEditorState() {
      public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return false;
      }
    };
  }

  public void writeState(@NotNull FileEditorState fileEditorState, @NotNull Project project, @NotNull Element element) {
  }

  @NotNull
  public String getEditorTypeId() {
    return "profile.view.provider";
  }

  @NotNull
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
  }

}
