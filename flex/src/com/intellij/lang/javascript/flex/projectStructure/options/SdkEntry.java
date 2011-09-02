package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.ex.ProjectRoot;
import com.intellij.openapi.projectRoots.impl.ProjectRootContainerImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.*;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.hash.LinkedHashMap;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author ksafonov
 */
public class SdkEntry implements JDOMExternalizable {

  private static final String HOME_ATTR = "home";
  private static final String DEPENDENCY_TYPE_ELEM = "entry";
  private static final String URL_ATTR = "url";
  @NotNull
  private String myHomePath;
  private final LinkedHashMap<String, DependencyType> myDependencyTypes = new LinkedHashMap<String, DependencyType>();

  @NotNull
  public String getHomePath() {
    return myHomePath;
  }

  public void setHomePath(@NotNull String homePath) {
    myHomePath = homePath;
    myDependencyTypes.clear();
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    String home = element.getAttributeValue(HOME_ATTR);
    if (home == null) {
      throw new InvalidDataException("home path should not be null");
    }
    myHomePath = home;

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
    copy.myDependencyTypes.clear();
    copy.myDependencyTypes.putAll(myDependencyTypes);
  }

  public boolean isEqual(@NotNull SdkEntry that) {
    if (!myHomePath.equals(that.myHomePath)) return false;
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
}
