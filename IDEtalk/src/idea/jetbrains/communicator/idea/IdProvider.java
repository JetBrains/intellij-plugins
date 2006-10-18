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
package jetbrains.communicator.idea;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * @author Kir
 */
public class IdProvider implements ProjectComponent, JDOMExternalizable {
  private static final Logger LOG = Logger.getLogger(IdProvider.class);

  public static final String ID = "IDEtalkID";
  private String myId;

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  public String getComponentName() {
    return "IdProvider";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public static IdProvider getInstance(Project project) {
    return project.getComponent(IdProvider.class);
  }

  public void readExternal(Element element) throws InvalidDataException {
    myId = element.getAttributeValue(ID);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    element.setAttribute(ID, getId());
  }

  public String getId() {
    if (myId == null) {
      Random random = new Random(System.currentTimeMillis() - hashCode());
      myId = md5(String.valueOf(random.nextDouble()));
    }
    return myId;
  }

  private static final char[] NUMS = new char[] {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };
  
  private String md5(String s) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte[] bytes = md5.digest(s.getBytes());
      char[] res = new char[bytes.length*2];
      for (int i = 0; i < bytes.length; i++) {
        res[2*i] = NUMS[(0xF0 & bytes[i]) >> 4];
        res[2*i + 1] = NUMS[0x0F & bytes[i]];
      }
      return new String(res);
    } catch (NoSuchAlgorithmException e) {
      LOG.error(e.getMessage(), e);
    }
    return null;
  }
}
