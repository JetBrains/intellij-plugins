package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.openapi.projectRoots.ex.ProjectRoot;
import com.intellij.openapi.projectRoots.ex.ProjectRootContainer;
import com.intellij.openapi.projectRoots.impl.ProjectRootContainerImpl;
import com.intellij.openapi.projectRoots.impl.SimpleProjectRoot;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.JarDirectories;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ksafonov
 */
public class ModuleLibraryEntry extends DependencyEntry implements JDOMExternalizable {

  @Nullable
  private String myName;
  private static final String NAME_ATTR = "name";

  private final ProjectRootContainerImpl myRootContainer;
  private final JarDirectories myJarDirectories;

  public ModuleLibraryEntry() {
    myRootContainer = new ProjectRootContainerImpl(true);
    myJarDirectories = new JarDirectories();
  }

  @Nullable
  public String getName() {
    return myName;
  }

  public void setName(@Nullable String name) {
    myName = name;
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    myName = element.getAttributeValue(NAME_ATTR);
    myRootContainer.readExternal(element);
    myJarDirectories.readExternal(element);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    if (myName != null) {
      element.setAttribute(NAME_ATTR, myName);
    }
    myRootContainer.writeExternal(element);
    myJarDirectories.writeExternal(element);
  }

  public VirtualFile[] getFiles(@NotNull OrderRootType rootType) {
    return getFiles(rootType, myRootContainer.getRootFiles(rootType), myJarDirectories);
  }

  public void addJarDirectory(OrderRootType rootType, String url, boolean recursive) {
    addRoot(rootType, url);
    myJarDirectories.add(rootType, url, recursive);
  }

  public static VirtualFile[] getFiles(OrderRootType rootType, VirtualFile[] rootFiles, JarDirectories jarDirectories) {
    final List<VirtualFile> expanded = new ArrayList<VirtualFile>();
    for (VirtualFile file : rootFiles) {
      if (file.isDirectory()) {
        if (jarDirectories.contains(rootType, file.getUrl())) {
          LibraryImpl.collectJarFiles(file, expanded, jarDirectories.isRecursive(rootType, file.getUrl()));
          continue;
        }
      }
      expanded.add(file);
    }
    return VfsUtil.toVirtualFileArray(expanded);
  }

  public void addRoot(OrderRootType rootType, VirtualFile root) {
    myRootContainer.startChange();
    myRootContainer.addRoot(root, rootType);
    myRootContainer.finishChange();
  }

  public void addRoot(OrderRootType rootType, String url) {
    myRootContainer.startChange();
    myRootContainer.addRoot(new SimpleProjectRoot(url), rootType);
    myRootContainer.finishChange();
  }

  public ProjectRoot[] getRoots(OrderRootType orderRootType) {
    return myRootContainer.getRoots(orderRootType);
  }

  public void removeAllRoots() {
    myRootContainer.startChange();
    myRootContainer.removeAllRoots();
    myRootContainer.finishChange();
    myJarDirectories.clear();
  }

  //public void removeRoot(String url, OrderRootType rootType) {
  //  myRootContainer.startChange();
  //  ProjectRoot[] roots = myRootContainer.getRoots(rootType);
  //  for (ProjectRoot root : roots) {
  //    if (url.equals(root.getUrls()[0])) {
  //      myRootContainer.removeRoot(root.getVirtualFiles()[0], rootType);
  //      break;
  //    }
  //  }
  //  myRootContainer.finishChange();
  //  myJarDirectories.remove(rootType, url);
  //}

  @Override
  public ModuleLibraryEntry getCopy() {
    ModuleLibraryEntry copy = new ModuleLibraryEntry();
    applyTo(copy);
    return copy;
  }

  @Override
  public void applyTo(DependencyEntry copy) {
    super.applyTo(copy);
    ModuleLibraryEntry libraryEntry = (ModuleLibraryEntry)copy;
    libraryEntry.myName = myName;
    copyContainer(myRootContainer, libraryEntry.myRootContainer);
    libraryEntry.myJarDirectories.copyFrom(myJarDirectories);
  }

  static void copyContainer(ProjectRootContainerImpl source, ProjectRootContainerImpl target) {
    target.startChange();
    target.removeAllRoots();
    for (OrderRootType rootType : OrderRootType.getAllTypes()) {
      ProjectRoot[] roots = source.getRoots(rootType);
      for (ProjectRoot root : roots) {
        for (VirtualFile file : root.getVirtualFiles()) {
          target.addRoot(file, rootType);
        }
      }
    }
    target.finishChange();
  }

  public boolean isEqual(ModuleLibraryEntry e) {
    if (!Comparing.equal(myName, e.myName)) return false;

    if (!isEqual(myRootContainer, e.myRootContainer)) return false;
    if (!myJarDirectories.equals(e.myJarDirectories)) return false;
    return true;
  }

  static boolean isEqual(ProjectRootContainer c1, ProjectRootContainer c2) {
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

  public boolean isJarDirectory(OrderRootType rootType, String url) {
    return myJarDirectories.contains(rootType, url);
  }

  public boolean isRecursive(OrderRootType rootType, String url) {
    return myJarDirectories.isRecursive(rootType, url);
  }

  // copied from LibraryImpl
  //private class MyRootProviderImpl extends RootProviderBaseImpl {
  //  @NotNull
  //  public String[] getUrls(@NotNull OrderRootType rootType) {
  //    Set<String> originalUrls = new LinkedHashSet<String>(Arrays.asList(ModuleLibraryEntry.this.getUrls(rootType)));
  //    for (VirtualFile file : getFiles(rootType)) { // Add those expanded with jar directories.
  //      originalUrls.add(file.getUrl());
  //    }
  //    return ArrayUtil.toStringArray(originalUrls);
  //  }
  //
  //  @NotNull
  //  public VirtualFile[] getFiles(@NotNull final OrderRootType rootType) {
  //    return ModuleLibraryEntry.this.getFiles(rootType);
  //  }
  //
  //  public void fireRootSetChanged() {
  //    super.fireRootSetChanged();
  //  }
  //}
}
