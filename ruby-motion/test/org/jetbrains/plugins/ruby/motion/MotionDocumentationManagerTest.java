package org.jetbrains.plugins.ruby.motion;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.jetbrains.cidr.DocSet;

public class MotionDocumentationManagerTest extends RubyMotionLightFixtureTestCase {
  public void testDocSetCaching() throws Exception {
    if (!SystemInfo.isMac) return;

    Project project = getProject();
    MotionDocumentationManager manager = new MotionDocumentationManager(project);
    DocSet docSet = manager.getDocSet(null);
    assertNotNull(docSet);
    assertSame(docSet, manager.getDocSet(null));
  }
}
