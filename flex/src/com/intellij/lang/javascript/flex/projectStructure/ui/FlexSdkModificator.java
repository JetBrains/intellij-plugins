package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.FlexSdkProperties;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public class FlexSdkModificator implements SdkModificator, Disposable {

  private final LibraryEx.ModifiableModelEx myModifiableModel;

  public FlexSdkModificator(LibraryEx.ModifiableModelEx modifiableModel) {
    myModifiableModel = modifiableModel;
  }

  @Override
  public void setName(String name) {
    myModifiableModel.setName(name);
  }

  @Override
  public String getName() {
    return myModifiableModel.getName();
  }

  @Override
  public void addRoot(@NotNull VirtualFile file, @NotNull OrderRootType rootType) {
    myModifiableModel.addRoot(file, rootType);
  }

  public void dispose() {
    Disposer.dispose(myModifiableModel);
  }

  @Override
  public String getHomePath() {
    return ((FlexSdkProperties)myModifiableModel.getProperties()).getHomePath();
  }

  @Override
  public void setHomePath(String path) {
    ((FlexSdkProperties)myModifiableModel.getProperties()).setHomePath(path);
  }

  @Override
  public String getVersionString() {
    return ((FlexSdkProperties)myModifiableModel.getProperties()).getVersion();
  }

  @Override
  public void setVersionString(String versionString) {
    ((FlexSdkProperties)myModifiableModel.getProperties()).setVersion(versionString);
  }

  @Override
  public SdkAdditionalData getSdkAdditionalData() {
    return null;
  }

  @Override
  public void setSdkAdditionalData(SdkAdditionalData data) {
  }

  @Override
  public VirtualFile[] getRoots(OrderRootType rootType) {
    return myModifiableModel.getFiles(rootType);
  }

  @Override
  public void removeRoot(VirtualFile root, OrderRootType rootType) {
    myModifiableModel.removeRoot(root.getUrl(), rootType);
  }

  @Override
  public void removeRoots(OrderRootType rootType) {
    String[] urls = myModifiableModel.getUrls(rootType);
    for (String url : urls) {
      myModifiableModel.removeRoot(url, rootType);
    }
  }

  @Override
  public void removeAllRoots() {
    for (OrderRootType rootType : OrderRootType.getAllTypes()) {
      removeRoots(rootType);
    }
  }

  @Override
  public void commitChanges() {
    String version = getVersionString();
    if (version != null) {
      setName("Flex SDK " + version);
    }
    myModifiableModel.commit();
  }

  @Override
  public boolean isWritable() {
    return true;
  }
}
