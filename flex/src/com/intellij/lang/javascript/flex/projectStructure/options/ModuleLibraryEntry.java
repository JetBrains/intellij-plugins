package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.openapi.projectRoots.ex.ProjectRoot;
import com.intellij.openapi.projectRoots.ex.ProjectRootContainer;
import com.intellij.openapi.projectRoots.impl.ProjectRootContainerImpl;
import com.intellij.openapi.projectRoots.impl.SimpleProjectRoot;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableImplUtil;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ksafonov
 */
public class ModuleLibraryEntry extends DependencyEntry {

  private static final String LIBRARY_ID_ATTR = "id";
  @NotNull
  private final String myLibraryId;

  public ModuleLibraryEntry(@NotNull String libraryId) {
    myLibraryId = libraryId;
  }

  @NotNull
  public String getLibraryId() {
    return myLibraryId;
  }

  public ModuleLibraryEntry(Element element) throws InvalidDataException {
    String id = element.getAttributeValue(LIBRARY_ID_ATTR);
    if (StringUtil.isEmpty(id)) {
      throw new InvalidDataException("empty library id");
    }
    myLibraryId = id;
  }

  public void writeExternal(Element element) throws WriteExternalException {
    element.setAttribute(LIBRARY_ID_ATTR, myLibraryId);
  }

  @Override
  public ModuleLibraryEntry getCopy() {
    ModuleLibraryEntry copy = new ModuleLibraryEntry(myLibraryId);
    myDependencyType.applyTo(copy.myDependencyType);
    return copy;
  }

  public static void copyContainer(ProjectRootContainerImpl source, ProjectRootContainerImpl target) {
    target.startChange();
    target.removeAllRoots();
    for (OrderRootType rootType : OrderRootType.getAllTypes()) {
      ProjectRoot[] roots = source.getRoots(rootType);
      for (ProjectRoot root : roots) {
        for (String url : root.getUrls()) {
          target.addRoot(new SimpleProjectRoot(url), rootType);
        }
      }
    }
    target.finishChange();
  }

  public static boolean isEqual(ProjectRootContainer c1, ProjectRootContainer c2) {
    for (OrderRootType rootType : OrderRootType.getAllTypes()) {
      ProjectRoot[] myRoots = c1.getRoots(rootType);
      ProjectRoot[] thatRoots = c2.getRoots(rootType);

      if (myRoots.length != thatRoots.length) return false;
      for (int i = 0; i < myRoots.length; i++) {
        VirtualFile[] myFiles = myRoots[i].getVirtualFiles();
        VirtualFile[] thatFiles = thatRoots[i].getVirtualFiles();
        if (!Comparing.equal(myFiles, thatFiles)) return false;
      }
    }
    return true;
  }

  @Nullable
  public LibraryOrderEntry findOrderEntry(ModuleRootModel rootModel) {
    for (OrderEntry orderEntry : rootModel.getOrderEntries()) {
      if (orderEntry instanceof LibraryOrderEntry) {
        if (!LibraryTableImplUtil.MODULE_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel())) {
          continue;
        }
        LibraryEx library = (LibraryEx)((LibraryOrderEntry)orderEntry).getLibrary();
        if (library == null || !(library.getType() instanceof FlexLibraryType)) {
          continue;
        }
        if (myLibraryId.equals(FlexProjectRootsUtil.getLibraryId(library))) {
          return (LibraryOrderEntry)orderEntry;
        }
      }
    }
    return null;
  }

}
