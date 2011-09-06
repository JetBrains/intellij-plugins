package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.hash.LinkedHashMap;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

/**
 * @author ksafonov
 */
public class SdkEntry {
  private static Logger LOG = Logger.getInstance(SdkEntry.class.getName());

  private static final String HOME_ATTR = "home";
  private static final String LIBRARY_ID_ATTR = "id";
  private static final String DEPENDENCY_TYPE_ELEM = "entry";
  private static final String URL_ATTR = "url";

  @NotNull
  private final String myLibraryId;

  @NotNull
  private final String myHomePath;

  private final LinkedHashMap<String, DependencyType> myDependencyTypes = new LinkedHashMap<String, DependencyType>();

  public SdkEntry(Element element) throws InvalidDataException {
    String libraryId = element.getAttributeValue(LIBRARY_ID_ATTR);
    if (StringUtil.isEmpty(libraryId)) {
      throw new InvalidDataException("library id is empty");
    }
    myLibraryId = libraryId;

    String home = element.getAttributeValue(HOME_ATTR);
    if (StringUtil.isEmpty(home)) {
      throw new InvalidDataException("home path is empty");
    }
    myHomePath = home;

    for (Object dependencyTypeElement : element.getChildren(DEPENDENCY_TYPE_ELEM)) {
      String url = ((Element)dependencyTypeElement).getAttributeValue(URL_ATTR);
      DependencyType dependencyType = new DependencyType();
      dependencyType.readExternal(((Element)dependencyTypeElement));
      myDependencyTypes.put(url, dependencyType);
    }
  }

  public SdkEntry(@NotNull String libraryId, String homePath) {
    myLibraryId = libraryId;
    myHomePath = homePath;
  }

  public void writeExternal(Element element) throws WriteExternalException {
    element.setAttribute(LIBRARY_ID_ATTR, myLibraryId);
    element.setAttribute(HOME_ATTR, myHomePath);
    for (Map.Entry<String, DependencyType> entry : myDependencyTypes.entrySet()) {
      Element dependencyTypeElement = new Element(DEPENDENCY_TYPE_ELEM);
      element.addContent(dependencyTypeElement);
      dependencyTypeElement.setAttribute(URL_ATTR, entry.getKey());
      entry.getValue().writeExternal(dependencyTypeElement);
    }
  }

  public SdkEntry getCopy() {
    SdkEntry copy = new SdkEntry(myLibraryId, myHomePath);
    copy.myDependencyTypes.clear();
    copy.myDependencyTypes.putAll(myDependencyTypes);
    return copy;
  }

  public boolean isEqual(@NotNull SdkEntry that) {
    if (!myLibraryId.equals(that.myLibraryId)) return false;
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

  @NotNull
  public String getLibraryId() {
    return myLibraryId;
  }

  @NotNull
  public String getHomePath() {
    return myHomePath;
  }

  @Nullable
  public Library findLibrary() {
    Library result = ContainerUtil.find(LibraryTablesRegistrar.getInstance().getLibraryTable().getLibraries(), new Condition<Library>() {
      @Override
      public boolean value(Library library) {
        return FlexSdk.isFlexSdk(library) && FlexProjectRootsUtil.getSdkLibraryId(library).equals(myLibraryId);
      }
    });
    if (result != null) {
      LOG.assertTrue(myHomePath.equals(FlexSdk.getHomePath(result)), "Unexpected home path");
    }
    return result;
  }
}
