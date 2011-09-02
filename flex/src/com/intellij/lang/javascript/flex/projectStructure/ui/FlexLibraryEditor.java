package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.options.ModuleLibraryEntry;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.projectRoots.ex.ProjectRoot;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.JarDirectories;
import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.ui.*;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerContainer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.HashMap;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

/**
 * @author ksafonov
 */
public class FlexLibraryEditor implements LibraryEditor, Disposable {

  private final Map<OrderRootType, VirtualFilePointerContainer> myRoots = new HashMap<OrderRootType, VirtualFilePointerContainer>();
  private final JarDirectories myJarDirectories = new JarDirectories();
  private final OrderRootType[] myEditableRootTypes;
  private String myName;

  public FlexLibraryEditor(ModuleLibraryEntry entry) {
    myName = entry.getName();
    myEditableRootTypes = OrderRootType.getAllTypes();

    for (OrderRootType rootType : OrderRootType.getAllTypes()) {
      myRoots.put(rootType, VirtualFilePointerManager.getInstance().createContainer(this));
    }

    for (OrderRootType rootType : OrderRootType.getAllTypes()) {
      ProjectRoot[] roots = entry.getRoots(rootType);
      for (ProjectRoot root : roots) {
        for (String url : root.getUrls()) {
          myRoots.get(rootType).add(url);
          if (entry.isJarDirectory(rootType, url)) {
            myJarDirectories.add(rootType, url, entry.isRecursive(rootType, url));
          }
        }
      }
    }
  }

  public FlexLibraryEditor(FlexSdk sdk) {
    myEditableRootTypes = FlexSdk.EDITABLE_ROOT_TYPES;
    myName = MessageFormat.format("Flex SDK {0}", sdk.getFlexVersion());
    for (OrderRootType rootType : FlexSdk.EDITABLE_ROOT_TYPES) {
      VirtualFilePointerContainer c = VirtualFilePointerManager.getInstance().createContainer(this);
      myRoots.put(rootType, c);
      for (String url : sdk.getRoots(rootType)) {
        c.add(url);
      }
    }
  }

  @Override
  public String getName() {
    return myName;
  }

  @Override
  public String[] getUrls(OrderRootType rootType) {
    if (!ArrayUtil.contains(rootType, myEditableRootTypes)) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
    return myRoots.get(rootType).getUrls();
  }

  @Override
  public VirtualFile[] getFiles(OrderRootType rootType) {
    if (!ArrayUtil.contains(rootType, myEditableRootTypes)) {
      return VirtualFile.EMPTY_ARRAY;
    }

    VirtualFile[] rootFiles = myRoots.get(rootType).getFiles();
    return ModuleLibraryEntry.getFiles(rootType, rootFiles, myJarDirectories);
  }

  @Override
  public void setName(String name) {
    myName = name;
  }

  @Override
  public void addRoot(VirtualFile file, OrderRootType rootType) {
    assertEditable(rootType);
    myRoots.get(rootType).add(file);
  }

  private void assertEditable(OrderRootType rootType) {
    if (!ArrayUtil.contains(rootType, myEditableRootTypes)) {
      throw new IllegalArgumentException("root type " + rootType + " is not editable");
    }
  }

  @Override
  public void addRoot(String url, OrderRootType rootType) {
    assertEditable(rootType);
    myRoots.get(rootType).add(url);
  }

  @Override
  public void addJarDirectory(VirtualFile file, boolean recursive, OrderRootType rootType) {
    assertEditable(rootType);
    addRoot(file, rootType);
    myJarDirectories.add(rootType, file.getUrl(), recursive);
  }

  @Override
  public void addJarDirectory(String url, boolean recursive, OrderRootType rootType) {
    assertEditable(rootType);
    addRoot(url, rootType);
    myJarDirectories.add(rootType, url, recursive);
  }

  @Override
  public void removeRoot(String url, OrderRootType rootType) {
    assertEditable(rootType);
    VirtualFilePointerContainer container = myRoots.get(rootType);
    VirtualFilePointer filePointer = container.findByUrl(url);
    if (filePointer != null) {
      container.remove(filePointer);
    }
  }

  @Override
  public void removeAllRoots() {
    for (VirtualFilePointerContainer container : myRoots.values()) {
      container.clear();
    }
    myJarDirectories.clear();
  }

  @Override
  public boolean hasChanges() {
    return true;
  }

  @Override
  public boolean isJarDirectory(String url, OrderRootType rootType) {
    return myJarDirectories.contains(rootType, url);
  }

  @Override
  public boolean isValid(String url, OrderRootType orderRootType) {
    VirtualFilePointer filePointer = myRoots.get(orderRootType).findByUrl(url);
    return filePointer != null && filePointer.isValid();
  }

  @Override
  public LibraryProperties getProperties() {
    return DummyLibraryProperties.INSTANCE;
  }

  @Override
  public LibraryType<?> getType() {
    return new FilteringLibraryType<DummyLibraryProperties>(LibraryType.EP_NAME.findExtension(FlexLibraryType.class), myEditableRootTypes);
  }

  @Override
  public void addRoots(Collection<? extends OrderRoot> roots) {
    for (OrderRoot root : roots) {
      if (root.isJarDirectory()) {
        addJarDirectory(root.getFile(), false, root.getType());
      }
      else {
        addRoot(root.getFile(), root.getType());
      }
    }
  }

  @Override
  public void dispose() {
  }

  public void applyTo(ModuleLibraryEntry entry) {
    entry.setName(myName);
    entry.removeAllRoots();

    for (OrderRootType rootType : OrderRootType.getAllTypes()) {
      String[] urls = myRoots.get(rootType).getUrls();
      for (String url : urls) {
        if (myJarDirectories.contains(rootType, url)) {
          entry.addJarDirectory(rootType, url, myJarDirectories.isRecursive(rootType, url));
        }
        else {
          entry.addRoot(rootType, url);
        }
      }
    }
  }

  public void applyTo(FlexSdk target) {
    for (OrderRootType rootType : myEditableRootTypes) {
      target.setRoots(rootType, myRoots.get(rootType).getUrls());
    }
  }

}
