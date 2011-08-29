package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.ex.ProjectRoot;
import com.intellij.openapi.projectRoots.impl.ProjectRootContainerImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.hash.LinkedHashMap;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

/**
 * @author ksafonov
 */
public class SdkEntry implements JDOMExternalizable {

  private static final String HOME_ATTR = "home";
  private static final String DEPENDENCY_TYPE_ELEM = "entry";
  private static final String URL_ATTR = "url";
  @NotNull
  private String myHomePath;
  private final ProjectRootContainerImpl myRoots = new ProjectRootContainerImpl(true);
  private final LinkedHashMap<String, DependencyType> myDependencyTypes = new LinkedHashMap<String, DependencyType>();
  @Nullable
  private String myFlexVersion;

  @NotNull
  public String getHomePath() {
    return myHomePath;
  }

  public void setHomePath(@NotNull String homePath) {
    myHomePath = homePath;
    myFlexVersion = null;
    detectRoots();
  }

  private void detectRoots() {
    VirtualFile sdkRoot = LocalFileSystem.getInstance().findFileByPath(myHomePath);
    FlexSdkUtils.setupSdkPaths(sdkRoot, FlexSdkType.getInstance(), new MySdkModificator());
    myDependencyTypes.clear();
    // TODO dependency types
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    String home = element.getAttributeValue(HOME_ATTR);
    if (home == null) {
      throw new InvalidDataException("home path should not be null");
    }
    myHomePath = home;
    myRoots.readExternal(element);

    myDependencyTypes.clear();
    for (Object dependencyTypeElement : element.getChildren(DEPENDENCY_TYPE_ELEM)) {
      String url = ((Element)dependencyTypeElement).getAttributeValue(URL_ATTR);
      DependencyType dependencyType = new DependencyType();
      dependencyType.readExternal(((Element)dependencyTypeElement));
      myDependencyTypes.put(url, dependencyType);
    }
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    element.setAttribute(HOME_ATTR, myHomePath);
    myRoots.writeExternal(element);
    for (Map.Entry<String, DependencyType> entry : myDependencyTypes.entrySet()) {
      Element dependencyTypeElement = new Element(DEPENDENCY_TYPE_ELEM);
      element.addContent(dependencyTypeElement);
      dependencyTypeElement.setAttribute(URL_ATTR, entry.getKey());
      entry.getValue().writeExternal(dependencyTypeElement);
    }
  }

  public SdkEntry getCopy() {
    SdkEntry copy = new SdkEntry();
    applyTo(copy);
    return copy;
  }

  private void applyTo(SdkEntry copy) {
    copy.myHomePath = myHomePath;
    ModuleLibraryEntry.copyContainer(myRoots, copy.myRoots);
    copy.myDependencyTypes.clear();
    copy.myDependencyTypes.putAll(myDependencyTypes);
  }

  @NotNull
  public String detectFlexVersion() {
    if (myFlexVersion == null) {
      VirtualFile sdkRoot = LocalFileSystem.getInstance().findFileByPath(myHomePath);
      myFlexVersion = FlexSdkUtils.readFlexSdkVersion(sdkRoot);
    }
    return myFlexVersion;
  }

  public boolean isEqual(@NotNull SdkEntry that) {
    if (!myHomePath.equals(that.myHomePath)) return false;
    if (ModuleLibraryEntry.isEqual(myRoots, that.myRoots)) return false;
    Iterator<String> i1 = myDependencyTypes.keySet().iterator();
    Iterator<String> i2 = that.myDependencyTypes.keySet().iterator();
    while (i1.hasNext() && i2.hasNext()) {
      String url1 = i1.next();
      String url2 = i2.next();
      if (!url1.equals(url2)) return true;
      if (!Comparing.equal(myDependencyTypes.get(url1), myDependencyTypes.get(url2))) return true;
    }
    if (i1.hasNext() || i2.hasNext()) return false;
    return true;
  }

  private class MySdkModificator implements SdkModificator {

    private MySdkModificator() {
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
}
