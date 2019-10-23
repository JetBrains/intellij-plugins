// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.vfs.ProjectsData;
import org.jdom.Element;

import java.util.List;

/**
 * @author Kir
 */
public class GetProjectsDataXmlMessage implements XmlMessage {
  public static final String TAG = "projectsData";
  private final ProjectsData[] myResult;

  public GetProjectsDataXmlMessage(ProjectsData[] result) {
    myResult = result;
  }

  @Override
  public String getTagName() {
    return TAG;
  }

  @Override
  public String getTagNamespace() {
    return Transport.NAMESPACE;
  }

  @Override
  public boolean needsResponse() {
    return true;
  }

  @Override
  public void fillRequest(Element element) {
  }

  @Override
  public void processResponse(Element responseElement) {
    List children = responseElement.getChildren();
    if (children.size() > 0) {
      ProjectsData projectsData = new ProjectsData((Element) children.get(0));
      if (projectsData.getProjects().length > 0) {
        myResult[0] = projectsData;
      }
    }
  }
}
