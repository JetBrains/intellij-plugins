package com.jetbrains.profiler;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProfileFileTypeFactory extends FileTypeFactory {
  final static FileType instance = new FileType() {
    @Override
    @NotNull
    public String getName() {
      return "Snapshot";
    }

    @Override
    @NotNull
    public String getDescription() {
      return "Profiler Snapshot";
    }

    @Override
    @NotNull
    public String getDefaultExtension() {
      return "";
    }

    @Override
    public Icon getIcon() {
      return AllIcons.Actions.ProfileCPU;
    }

    @Override
    public boolean isBinary() {
      return true;
    }

    @Override
    public boolean isReadOnly() {
      return false;
    }

    @Override
    public String getCharset(@NotNull VirtualFile virtualFile, @NotNull byte[] bytes) {
      return null;
    }
  };

  @Override
  public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
    fileTypeConsumer.consume(instance, "snapshot");
  }
}
