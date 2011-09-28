package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkProperties;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectRootUtil;
import com.intellij.openapi.projectRoots.impl.SimpleProjectRoot;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.JarDirectories;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.HashMap;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;

import java.util.*;

/**
 * User: ksafonov
 */
public class ConverterFlexSdkModificator implements SdkModificator {
  private final Element myLibraryElement;
  private final FlexSdkProperties myProperties;
  private final Map<OrderRootType, LinkedHashSet<String>> myRoots = new HashMap<OrderRootType, LinkedHashSet<String>>();

  public ConverterFlexSdkModificator(String homePath, String libraryId) {
    myLibraryElement = new Element(LibraryImpl.ELEMENT);
    myProperties = new FlexSdkProperties(libraryId);
    myProperties.setHomePath(homePath);
  }

  @Override
  public String getName() {
    return myLibraryElement.getAttributeValue(LibraryImpl.LIBRARY_NAME_ATTR);
  }

  @Override
  public void setName(String name) {
    myLibraryElement.setAttribute(LibraryImpl.LIBRARY_NAME_ATTR, name);
  }

  @Override
  public String getHomePath() {
    return myProperties.getHomePath();
  }

  @Override
  public void setHomePath(String path) {
    myProperties.setHomePath(path);
  }

  @Override
  public String getVersionString() {
    return myProperties.getVersion();
  }

  @Override
  public void setVersionString(String versionString) {
    myProperties.setVersion(versionString);
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
    return new VirtualFile[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void addRoot(VirtualFile root, OrderRootType rootType) {
    LinkedHashSet<String> roots = myRoots.get(rootType);
    if (roots == null) {
      roots = new LinkedHashSet<String>();
      myRoots.put(rootType, roots);
    }
    roots.add(root.getUrl());
  }

  @Override
  public void removeRoot(VirtualFile root, OrderRootType rootType) {
    LinkedHashSet<String> roots = myRoots.get(rootType);
    if (roots != null) {
      roots.remove(root.getUrl());
      if (roots.isEmpty()) {
        myRoots.remove(rootType);
      }
    }
  }

  @Override
  public void removeRoots(OrderRootType rootType) {
    myRoots.remove(rootType);
  }

  @Override
  public void removeAllRoots() {
    myRoots.clear();
  }

  @Override
  public boolean isWritable() {
    return true;
  }

  @Override
  public void commitChanges() {
    myLibraryElement.setAttribute(LibraryImpl.LIBRARY_NAME_ATTR, FlexSdkType.suggestSdkName(myProperties.getHomePath(), (SdkType)null));
    myLibraryElement.setAttribute(LibraryImpl.LIBRARY_TYPE_ATTR, FlexSdkLibraryType.FLEX_SDK.getKindId());
    Element propertiesElement = new Element(LibraryImpl.PROPERTIES_ELEMENT);
    XmlSerializer.serializeInto(myProperties, propertiesElement);
    myLibraryElement.addContent(propertiesElement);

    for (OrderRootType rootType : LibraryImpl.sortRootTypes(Arrays.asList(OrderRootType.getAllTypes()))) {
      LinkedHashSet<String> roots = myRoots.get(rootType);
      if (roots != null) {
        Element rootTypeElement = new Element(rootType.name());
        myLibraryElement.addContent(rootTypeElement);
        final List<String> urls = new ArrayList<String>(roots);
        Collections.sort(urls, String.CASE_INSENSITIVE_ORDER);
        for (String url : urls) {
          final Element jarDirElement = new Element(ProjectRootUtil.ELEMENT_ROOT);
          jarDirElement.setAttribute(SimpleProjectRoot.ATTRIBUTE_URL, url);
          rootTypeElement.addContent(jarDirElement);
        }
      }
    }
  }

  public Element getLibraryElement() {
    return myLibraryElement;
  }
}
