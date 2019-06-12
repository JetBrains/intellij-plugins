// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.io.DigestUtil;
import org.jdom.Element;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

public class IdProvider implements JDOMExternalizable {
  public static final String ID = "IDEtalkID";
  private String myId;

  public static IdProvider getInstance(Project project) {
    return project.getComponent(IdProvider.class);
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    myId = element.getAttributeValue(ID);
  }

  @Override
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

  private static final char[] NUMS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };

  private static String md5(String s) {
    MessageDigest md5 = DigestUtil.md5();
    byte[] bytes = md5.digest(s.getBytes(StandardCharsets.UTF_8));
    char[] res = new char[bytes.length*2];
    for (int i = 0; i < bytes.length; i++) {
      res[2*i] = NUMS[(0xF0 & bytes[i]) >> 4];
      res[2*i + 1] = NUMS[0x0F & bytes[i]];
    }
    return new String(res);
  }
}
