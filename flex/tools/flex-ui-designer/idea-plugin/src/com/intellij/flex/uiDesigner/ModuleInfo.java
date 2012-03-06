package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.flex.uiDesigner.io.AmfOutputable;
import com.intellij.flex.uiDesigner.io.Info;
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

  public void addLocalStyleHolder(LocalStyleHolder localStyleHolder) {
    if (localStyleHolders == null) {
      localStyleHolders = new ArrayList<LocalStyleHolder>(5);
    }

    localStyleHolders.add(localStyleHolder);
  }

  @Override
  public void dispose() {
    // need only for message bus connections
  }
}

class LocalStyleHolder implements AmfOutputable {
  private final VirtualFile file;
  private final byte[] data;

  LocalStyleHolder(VirtualFile file, byte[] data) {
    this.file = file;
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
}

class ExternalLocalStyleHolder extends LocalStyleHolder {
  final List<VirtualFile> users;

  ExternalLocalStyleHolder(VirtualFile file, byte[] data, VirtualFile user) {
    super(file, data);

    users = new ArrayList<VirtualFile>(5);
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

