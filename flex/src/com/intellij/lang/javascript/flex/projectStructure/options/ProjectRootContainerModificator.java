package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.ex.ProjectRoot;
import com.intellij.openapi.projectRoots.impl.ProjectRootContainerImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * User: ksafonov
 */
public class ProjectRootContainerModificator implements SdkModificator {

  private final String myHomePath;
  private final ProjectRootContainerImpl myRoots;

  public ProjectRootContainerModificator(String homePath, ProjectRootContainerImpl roots) {
    myHomePath = homePath;
    myRoots = roots;
    myRoots.startChange();
    myRoots.removeAllRoots();
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void setName(String name) {
  }

  @Override
  public String getHomePath() {
    return myHomePath;
  }

  @Override
  public void setHomePath(String path) {
  }

  @Override
  public String getVersionString() {
    return null;
  }

  @Override
  public void setVersionString(String versionString) {
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
    return myRoots.getRootFiles(rootType);
  }

  @Override
  public void addRoot(VirtualFile root, OrderRootType rootType) {
    myRoots.addRoot(root, rootType);
  }

  @Override
  public void removeRoot(VirtualFile root, OrderRootType rootType) {
    myRoots.removeRoot(root, rootType);
  }

  @Override
  public void removeRoots(OrderRootType rootType) {
    ProjectRoot[] roots = myRoots.getRoots(rootType);
    for (ProjectRoot root : roots) {
      myRoots.removeRoot(root, rootType);
    }
  }

  @Override
  public void removeAllRoots() {
    myRoots.removeAllRoots();
  }

  @Override
  public void commitChanges() {
    myRoots.finishChange();
  }

  @Override
  public boolean isWritable() {
    return true;
  }
}
