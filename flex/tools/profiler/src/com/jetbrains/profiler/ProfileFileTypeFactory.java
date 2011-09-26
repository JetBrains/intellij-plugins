package com.jetbrains.profiler;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: Maxim
 * Date: 21.09.2010
 * Time: 20:24:42
 */
public class ProfileFileTypeFactory extends FileTypeFactory {
  final static FileType instance = new FileType() {
    @NotNull
    public String getName() {
      return "Snapshot";
    }

    @NotNull
    public String getDescription() {
      return "Profiler Snapshot";
    }

    @NotNull
    public String getDefaultExtension() {
      return "";
    }

    public Icon getIcon() {
      return DefaultProfilerExecutor.ICON;
    }

    public boolean isBinary() {
      return true;
    }

    public boolean isReadOnly() {
      return false;
    }

    public String getCharset(@NotNull VirtualFile virtualFile, byte[] bytes) {
      return null;
    }
  };
  
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
    fileTypeConsumer.consume(instance, "snapshot");
  }
}
