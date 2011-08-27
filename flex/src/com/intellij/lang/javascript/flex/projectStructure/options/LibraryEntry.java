package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.ex.ProjectRoot;
import com.intellij.openapi.projectRoots.impl.ProjectRootContainerImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

/**
 * @author ksafonov
 */
public class LibraryEntry extends DependencyEntry implements JDOMExternalizable {

  @Nullable
  private String myName;
  private static final String NAME_ATTR = "name";

  private final ProjectRootContainerImpl myRootContainer;

  public LibraryEntry() {
    myRootContainer = new ProjectRootContainerImpl(true);
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
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    if (myName != null) {
      element.setAttribute(NAME_ATTR, myName);
    }
    myRootContainer.writeExternal(element);
  }

  public void addRoot(VirtualFile root, OrderRootType orderRootType) {
    myRootContainer.startChange();
    myRootContainer.addRoot(root, orderRootType);
    myRootContainer.finishChange();
  }

  public ProjectRoot[] getRoots(OrderRootType orderRootType) {
    return myRootContainer.getRoots(orderRootType);
  }

  public LibraryEntry getCopy() {
    LibraryEntry copy = new LibraryEntry();
    applyTo(copy);
    return copy;
  }

  public void applyTo(LibraryEntry copy) {
    copy.myName = myName;
    copy.myRootContainer.startChange();
    copy.myRootContainer.removeAllRoots();
    for (OrderRootType rootType : OrderRootType.getAllTypes()) {
      ProjectRoot[] roots = myRootContainer.getRoots(rootType);
      for (ProjectRoot root : roots) {
        for (VirtualFile file : root.getVirtualFiles()) {
          copy.myRootContainer.addRoot(file, rootType);
        }
      }
    }
  }

  public boolean isEqual(LibraryEntry e) {
    if (!Comparing.equal(myName, e.myName)) return false;

    for (OrderRootType rootType : OrderRootType.getAllTypes()) {
      ProjectRoot[] myRoots = myRootContainer.getRoots(rootType);
      ProjectRoot[] thatRoots = e.myRootContainer.getRoots(rootType);

      if (myRoots.length != thatRoots.length) return false;
      for (int i = 0; i < myRoots.length; i++) {
        VirtualFile[] myFiles = myRoots[i].getVirtualFiles();
        VirtualFile[] thatFiles = thatRoots[i].getVirtualFiles();
        if (!Comparing.equal(myFiles, thatFiles)) return false;
      }
    }
    return true;
  }
}
