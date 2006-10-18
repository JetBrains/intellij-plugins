/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.communicator.core.vfs;

import jetbrains.communicator.util.StringUtil;
import junit.framework.Assert;
import org.jdom.Element;

import java.util.List;
import java.util.Vector;

/**
 * @author Kir
 *
 */
public class VFile implements Comparable, Cloneable {
  private String myContentPath;
  private final String myFullPath;
  private final boolean myWritable;

  private String myProjectName;
  private String myContents;
  private String myName;
  private String mySourcePath;
  private String myFQName;
  public static final String ELEMENT_NAME = "VFile";
  public static final String FULL_PATH_ATTR = "fullPath";
  public static final String CONTENT_PATH_ATTR = "contentPath";
  public static final String WRITABLE_ATTR = "writable";
  public static final String PROJECT_NAME_ATTR = "projectName";
  public static final String FQNAME_ATTR = "fqName";
  public static final String SOURCE_PATH_ATTR = "sourcePath";
  public static final String HAS_CONTENTS_ATTR = "hasContents";

  public static VFile create(String fullPath, String contentPath, boolean writable) {
    assert fullPath != null: "full path is null";
    VFile file = create(fullPath, writable);
    file.setContentPath(contentPath);
    return file;
  }

  public static VFile create(String path, boolean writable) {
    VFile file = new VFile(path, writable, null, null);
    file.setContentPath(path);
    return file;
  }

  public static VFile create(String path) {
    return create(path, true);
  }

  private VFile(String path, boolean writable, String project, String contents) {
    if (path == null) {
      throw new IllegalArgumentException("Path cannot be null");
    }
    myFullPath = path;
    myWritable = writable;
    myProjectName = project;
    myContents = contents;
  }

  private String getShortestPath() {
    if (mySourcePath != null) return mySourcePath;
    if (myContentPath != null) return myContentPath;
    return myFullPath;
  }

  public String getName() {
    if (myName == null) {
      String path = getShortestPath();
      int lastSlash = path.lastIndexOf('/');
      lastSlash = Math.max(lastSlash, path.lastIndexOf('\\'));
      if (lastSlash == -1) {
        myName = path;
      }
      else {
        myName = path.substring(lastSlash + 1);
      }
    }
    return myName;
  }

  public String getContentPath() {
    return myContentPath;
  }

  public String getFullPath() {
    return myFullPath;
  }

  public void setContentPath(String path) {
    myContentPath = path;
  }

  public boolean isWritable() {
    return myWritable;
  }

  public int compareTo(Object o) {
    return getShortestPath().compareTo(((VFile)o).getShortestPath());
  }

  /** Returns contents of the remote file, if it was set */
  public String getContents() {
    return myContents;
  }

  /** returns a project this file belongs to. Can be null if file
   * does not belong to any project */
  public String getProjectName() {
    return myProjectName;
  }

  public void setContents(String contents) {
    myContents = contents;
  }

  public void setProjectName(String projectName) {
    myProjectName = projectName;
  }

  public String getDisplayName() {
    if (getProjectName() == null) {
      return myContentPath;
    }
    return '[' +getProjectName()+"] " + myContentPath;
  }

  public String getSourcePath() {
    return mySourcePath;
  }

  public void setSourcePath(String sourcePath) {
    mySourcePath = sourcePath;
  }

  /*** For Java files, returns Full Qualified Name of the first class containing in the file */
  public String getFQName() {
    return myFQName;
  }

  public void setFQName(String FQName) {
    myFQName = FQName;
  }

  public Vector asVector() {
    Vector vector = new Vector();
    vector.add(getFullPath());
    vector.add(Boolean.valueOf(isWritable()));
    vector.add(getContentPath() == null ? "" : getContentPath());
    vector.add(getProjectName() == null ? "" : getProjectName());
    vector.add(getSourcePath() == null ? "" : getSourcePath());
    vector.add(getFQName() == null ? "" : getFQName());
    if (getContents() != null) {
      vector.add(StringUtil.toXMLSafeString(getContents()));
    }
    return vector;
  }

  public static VFile createFromList(List v) {
    VFile result = create((String) v.get(0), ((Boolean) v.get(1)).booleanValue());
    result.setContentPath(getNullableItem(v, 2));
    result.setProjectName(getNullableItem(v, 3));
    result.setSourcePath(getNullableItem(v, 4));
    result.setFQName(getNullableItem(v, 5));
    if (v.size() > 6) {
      result.setContents(StringUtil.fromXMLSafeString(v.get(6).toString()));
    }
    return result;
  }

  public void saveTo(Element root) {
    setAttr(root, FULL_PATH_ATTR, getFullPath());
    setAttr(root, CONTENT_PATH_ATTR, getContentPath());
    setAttr(root, WRITABLE_ATTR, isWritable() ? "true" : "false");
    setAttr(root, FQNAME_ATTR, getFQName());
    setAttr(root, PROJECT_NAME_ATTR, getProjectName());
    setAttr(root, SOURCE_PATH_ATTR, getSourcePath());

    root.setAttribute(HAS_CONTENTS_ATTR, myContents == null ? "false" : "true");
    root.setText(getContents());
  }

  /** @Nullable */
  public static VFile createFrom(Element root) {
    String fullPath = root.getAttributeValue(FULL_PATH_ATTR);
    if (fullPath == null) return null;
    String contentPath = root.getAttributeValue(CONTENT_PATH_ATTR);
    String writable = root.getAttributeValue(WRITABLE_ATTR);
    VFile file = create(fullPath, contentPath, Boolean.valueOf(writable).booleanValue());
    file.setFQName(root.getAttributeValue(FQNAME_ATTR));
    file.setProjectName(root.getAttributeValue(PROJECT_NAME_ATTR));
    file.setSourcePath(root.getAttributeValue(SOURCE_PATH_ATTR));
    if ("true".equals(root.getAttributeValue(HAS_CONTENTS_ATTR))) {
      file.setContents(root.getText());
    }
    return file;
  }

  public boolean containsSearchString(String searchString) {
    return
        StringUtil.containedIn(getContentPath(), searchString) ||
            StringUtil.containedIn(getSourcePath(), searchString) ||
            StringUtil.containedIn(getFullPath(), searchString) ||
            StringUtil.containedIn(getFQName(), searchString);
  }

  private static String getNullableItem(List v, int index) {
    if (StringUtil.isEmpty((String) v.get(index))) return null;
    return v.get(index).toString();
  }

  public final Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public String toString() {
    return myContentPath + ' ' + myProjectName;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final VFile vFile = (VFile) o;

    if (myFullPath != null ? !myFullPath.equals(vFile.myFullPath) : vFile.myFullPath != null) return false;
    if (myProjectName != null ? !myProjectName.equals(vFile.myProjectName) : vFile.myProjectName != null) return false;

    return true;
  }

  public int hashCode() {
    return (myProjectName != null ? myProjectName.hashCode() : 0);
  }

  private void setAttr(Element root, String attrName, String val) {
    if (val != null) {
      root.setAttribute(attrName, val);
    }
  }

  public void assertEquals(VFile compareWith) {
    Assert.assertEquals(this, compareWith);
    Assert.assertEquals("Bad contents", getContents(), compareWith.getContents());
    Assert.assertEquals("Bad FQN", getFQName(), compareWith.getFQName());
    Assert.assertEquals("Bad projectName", getProjectName(), compareWith.getProjectName());
    Assert.assertEquals("Bad sourcePath", getSourcePath(), compareWith.getSourcePath());
  }
}
