package com.jetbrains.profiler;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class ProfileViewProvider implements FileEditorProvider, DumbAware {

  @Override
  public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    for(ProfilerSnapshotProvider provider:ProfilerSnapshotProvider.ProfileSnapshotProvider_EP.getExtensions()) {
      if (provider.accepts(virtualFile)) return true;
    }
    return false;
  }

  @Override
  @NotNull
  public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    for(ProfilerSnapshotProvider provider:ProfilerSnapshotProvider.ProfileSnapshotProvider_EP.getExtensions()) {
      if (provider.accepts(virtualFile)) return provider.createView(virtualFile, project);
    }
    assert false;
    return null;
  }

  @Override
  @NotNull
  public String getEditorTypeId() {
    return "profile.view.provider";
  }

  @Override
  @NotNull
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
  }

}
