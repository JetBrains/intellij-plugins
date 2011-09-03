package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.options.ModuleLibraryEntry;
import com.intellij.lang.javascript.flex.projectStructure.options.ProjectRootContainerModificator;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.ex.ProjectRoot;
import com.intellij.openapi.projectRoots.impl.ProjectRootContainerImpl;
import com.intellij.openapi.projectRoots.impl.SimpleProjectRoot;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author ksafonov
 */
public class FlexSdk {

  public static final String SDK_ELEM = "sdk";
  private static final String HOME_ATTR = "home";
  public static final OrderRootType[] EDITABLE_ROOT_TYPES = new OrderRootType[]{OrderRootType.SOURCES, JavadocOrderRootType.getInstance()};

  @NotNull
  private final String myHomePath;

  @Nullable
  private String myFlexVersion;

  @Nullable
  private Boolean myValidFlag;

  private final ProjectRootContainerImpl myRoots = new ProjectRootContainerImpl(true);

  public FlexSdk(@NotNull String homePath) {
    myHomePath = homePath;
  }

  public FlexSdk(Element element) throws InvalidDataException {
    String homePath = element.getAttributeValue(HOME_ATTR);
    if (StringUtil.isEmpty(homePath)) {
      throw new InvalidDataException("SDK home path is not defined");
    }
    myHomePath = homePath;
    myRoots.readExternal(element);
  }

  public Element getElement() throws WriteExternalException {
    Element element = new Element(SDK_ELEM);
    element.setAttribute("home", myHomePath);
    myRoots.writeExternal(element);
    return element;
  }

  public FlexSdk getCopy() {
    FlexSdk copy = new FlexSdk(myHomePath);
    ModuleLibraryEntry.copyContainer(myRoots, copy.myRoots);
    return copy;
  }

  @NotNull
  public String getHomePath() {
    return myHomePath;
  }

  public String[] getRoots(OrderRootType rootType) {
    Collection<String> urls = new ArrayList<String>();
    for (ProjectRoot root : myRoots.getRoots(rootType)) {
      urls.addAll(Arrays.asList(root.getUrls()));
    }
    return ArrayUtil.toStringArray(urls);
  }

  @NotNull
  public String getFlexVersion() {
    if (myFlexVersion == null) {
      VirtualFile sdkRoot = LocalFileSystem.getInstance().findFileByPath(myHomePath);
      myFlexVersion = FlexSdkUtils.readFlexSdkVersion(sdkRoot);
    }
    return myFlexVersion;
  }

  public SdkModificator createRootsModificator() {
    return new ProjectRootContainerModificator(myHomePath, myRoots);
  }

  public boolean isValid() {
    if (myValidFlag == null) {
      VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(myHomePath);
      myValidFlag = Boolean.valueOf(vFile != null && FlexSdkUtils.isValidSdkRoot(FlexIdeUtils.getSdkType(), vFile));
    }
    return myValidFlag.booleanValue();
  }

  public void setRoots(OrderRootType rootType, String[] urls) {
    myRoots.startChange();
    myRoots.removeAllRoots(rootType);
    for (String url : urls) {
      myRoots.addRoot(new SimpleProjectRoot(url), rootType);
    }
    myRoots.finishChange();
  }

  public boolean equalsTo(FlexSdk other) {
    if (!myHomePath.equals(other.myHomePath)) return false;
    if (!ModuleLibraryEntry.isEqual(myRoots, other.myRoots)) return false;
    return true;
  }
}
