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

import com.intellij.util.ArrayUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * @author Kir
 */
public class ProjectsData {
  private static final Logger LOG = Logger.getLogger(ProjectsData.class);
  public static final ProjectsData NULL = new ProjectsData();
  private final Hashtable<String, Vector<List>> myStatus;
  public static final String NON_PROJECT_KEY = "non project files";

  public ProjectsData() {
    this(new Hashtable<String, Vector<List>>());
  }

  public ProjectsData(Hashtable<String,Vector<List>> serializedStatus) {
    assert serializedStatus != null;
    myStatus = serializedStatus;
  }

  public ProjectsData(Element rootElement) {
    this(initFrom(rootElement));
  }

  public String[] getProjects() {
    Set<String> set = new HashSet<String>(myStatus.keySet());
    set.remove(NON_PROJECT_KEY);
    return ArrayUtil.toStringArray(set);
  }

  private static Hashtable<String,Vector<List>> initFrom(Element rootElement) {
    String s = new XMLOutputter().outputString(rootElement);
    return (Hashtable<String,Vector<List>>) new XStream(new DomDriver()).fromXML(s);
  }

  public Element serialize() {
    XStream xStream = new XStream(new DomDriver());
    String s = xStream.toXML(myStatus);
    try {
      return new SAXBuilder().build(new StringReader(s)).getRootElement();
    } catch (JDOMException e) {
      LOG.error(e.getMessage(), e);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    return new Element("");
  }

  public void setProjectFiles(String projectName, VFile[] fileInfos) {
    Vector<List> files = getFilesVector(projectName);
    files.clear();
    for (VFile fileInfo : fileInfos) {
      fileInfo.setProjectName(projectName);
      addFileInfo(files, fileInfo);
    }
  }

  private Vector<List> getFilesVector(String projectName) {
    Vector<List> files = myStatus.get(projectName);
    if (files == null) {
      files = new Vector<List>();
      myStatus.put(projectName, files);
    }
    return files;
  }

  public VFile[] getProjectFiles(String projectName) {
    Vector<List> files = myStatus.get(projectName);
    if (files == null) return new VFile[0];

    List<VFile> result = new ArrayList<VFile>(files.size());
    for (List fileData : files) {
      result.add(VFile.createFromList(fileData));
    }
    return result.toArray(new VFile[result.size()]);
  }

  public void addNonProjectFile(VFile fileInfo) {
    fileInfo.setProjectName(null);
    addFileInfo(getFilesVector(NON_PROJECT_KEY), fileInfo);
  }

  public VFile[] getNonProjectFiles() {
    return getProjectFiles(NON_PROJECT_KEY);
  }

  private void addFileInfo(Vector<List> files, VFile fileInfo) {
    files.add(fileInfo.asVector());
  }

  public boolean isEmpty() {
    return myStatus.size() == 0;
  }
}
