package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.ui.OrderRoot;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collection;

/**
 * User: ksafonov
 */
public class FlexSdkEditor implements LibraryEditor {

  @Override
  public String getName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String[] getUrls(OrderRootType rootType) {
    return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public VirtualFile[] getFiles(OrderRootType rootType) {
    return new VirtualFile[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setName(String name) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void addRoot(VirtualFile file, OrderRootType rootType) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void addRoot(String url, OrderRootType rootType) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void addJarDirectory(VirtualFile file, boolean recursive, OrderRootType rootType) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void addJarDirectory(String url, boolean recursive, OrderRootType rootType) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void removeRoot(String url, OrderRootType rootType) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void removeAllRoots() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean hasChanges() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isJarDirectory(String url, OrderRootType rootType) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isValid(String url, OrderRootType orderRootType) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public LibraryProperties getProperties() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public LibraryType<?> getType() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void addRoots(Collection<? extends OrderRoot> roots) {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
