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

import junit.framework.TestCase;

import java.util.Vector;

/**
 * @author Kir
 */
public class VFileTest extends TestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testSerializeDeserialize() {
    doSerializeTest("a path", "project",
                    "\u0441\u043e\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u0435 \u043f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0438\u0445 \u0441\u0435\u0440\u0438\u0439", false, null);
    doSerializeTest("a path", null,
                    "\u0441\u043e\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u0435 \u043f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0438\u0445 \u0441\u0435\u0440\u0438\u0439", true, null);
    doSerializeTest("a path", null, null, true, null);
    doSerializeTest("a path", "project", null, true, null);

    doSerializeTest("a path", "project", null, true, "some/path");
  }

  public void testNullContentPath() {
    VFile file = VFile.create("path", true);
    file.setContentPath(null);

    VFile res = doSerializeDeserialize(file);
    assertNull("Wrong path", res.getContentPath());
  }

  public void testFQName() {
    VFile file = VFile.create("path", true);
    file.setFQName("a.b.x");

    VFile res = doSerializeDeserialize(file);
    assertEquals("Wrong FQName", "a.b.x", res.getFQName());
  }

  private void doSerializeTest(String path, String project, String contents, boolean writable, String sourcePath) {
    VFile file = VFile.create(path, writable);
    file.setProjectName(project);
    file.setContents(contents);
    file.setSourcePath(sourcePath);

    VFile res = doSerializeDeserialize(file);
    assertEquals("Wrong path", path, res.getContentPath());
    assertEquals("Wrong project", project, res.getProjectName());
    assertEquals("Wrong contents", contents, res.getContents());
    assertEquals("Wrong writable status", writable, res.isWritable());
    assertEquals("Wrong source path", sourcePath, res.getSourcePath());
  }

  private VFile doSerializeDeserialize(VFile file) {
    Vector v = file.asVector();
    for (int i = 0; i < v.size(); i++) {
      assertNotNull("Nulls are not allowed in XML-RPC:" + i, v.get(i));

    }
    VFile res = VFile.createFromList(v);
    return res;
  }
}
