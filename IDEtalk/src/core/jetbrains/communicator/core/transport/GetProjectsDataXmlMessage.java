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

package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.vfs.ProjectsData;
import org.jdom.Element;

import java.util.List;

/**
 * @author Kir
 */
public class GetProjectsDataXmlMessage implements XmlMessage {
  public static final String TAG = "projectsData";
  private ProjectsData[] myResult;

  public GetProjectsDataXmlMessage(ProjectsData[] result) {
    myResult = result;
  }

  public String getTagName() {
    return TAG;
  }

  public String getTagNamespace() {
    return Transport.NAMESPACE;
  }

  public boolean needsResponse() {
    return true;
  }

  public void fillRequest(Element element) {
  }

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
