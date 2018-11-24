package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.flex.uiDesigner.io.AmfOutputable;
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.Info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleInfo extends Info<Module> implements Disposable {
  private List<LocalStyleHolder> localStyleHolders;
  private final LibrarySet librarySet;
  private final boolean app;

  public ModuleInfo(Module module, LibrarySet librarySet, boolean isApp) {
    super(module);
    this.librarySet = librarySet;
    app = isApp;
  }

  public LibrarySet getLibrarySet() {
    return librarySet;
  }

  public FlexLibrarySet getFlexLibrarySet() {
    return librarySet instanceof FlexLibrarySet ? (FlexLibrarySet)librarySet : (FlexLibrarySet)librarySet.getParent();
  }

  public boolean isApp() {
    return app;
  }

  public Module getModule() {
    return element;
  }

  @Nullable
  public List<LocalStyleHolder> getLocalStyleHolders() {
    return localStyleHolders;
  }

  public void setLocalStyleHolders(@Nullable List<LocalStyleHolder> localStyleHolders) {
    this.localStyleHolders = localStyleHolders;
  }

  @Override
  public void dispose() {
    // need only for message bus connections
  }
}

class LocalStyleHolder implements AmfOutputable {
  final VirtualFile file;
  private byte[] data;

  LocalStyleHolder(VirtualFile file, byte[] data) {
    this.file = file;
    this.data = data;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  @Override
  public void writeExternal(AmfOutputStream out) {
    Client.writeVirtualFile(file, out);
    out.writeAmfByteArray(data);
    writeUsers(out);
  }

  protected void writeUsers(AmfOutputStream out) {
    out.write(0);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof LocalStyleHolder)) {
      return false;
    }

    LocalStyleHolder other = (LocalStyleHolder)obj;
    return file.equals(other.file) && Arrays.equals(data, other.data);
  }
}

class ExternalLocalStyleHolder extends LocalStyleHolder {
  final List<VirtualFile> users;

  ExternalLocalStyleHolder(@NotNull VirtualFile file, @NotNull byte[] data, @NotNull VirtualFile user) {
    super(file, data);

    users = new ArrayList<>(5);
    users.add(user);
  }

  public void addUser(VirtualFile user) {
    users.add(user);
  }

  @Override
  protected void writeUsers(AmfOutputStream out) {
    out.write(users.size());
    for (VirtualFile user : users) {
      Client.writeVirtualFile(user, out);
    }
  }
}

