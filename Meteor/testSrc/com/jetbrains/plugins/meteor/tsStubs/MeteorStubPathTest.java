package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.util.containers.ContainerUtil;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.List;


public class MeteorStubPathTest extends TestCase {

  public void testVersionNumber() {
    List<MeteorStubPath.VersionNumber> versions =
      ContainerUtil.newArrayList(create("meteor-v1.1.1.d.ts"), create("meteor-v0.1.d.ts"), create("meteor-v0.2.2.d.ts"), create("meteor-v0.2.d.ts"));
    Collections.sort(versions);

    assertName(versions, 0, "meteor-v0.1.d.ts");
    assertName(versions, 1, "meteor-v0.2.d.ts");
    assertName(versions, 2, "meteor-v0.2.2.d.ts");
    assertName(versions, 3, "meteor-v1.1.1.d.ts");
  }

  private static MeteorStubPath.VersionNumber create(String name) {
    return new MeteorStubPath.VersionNumber(name);
  }

  private static void assertName(List<MeteorStubPath.VersionNumber> versions, int index,String name) {
    assertEquals(name, versions.get(index).myFileName);
  }

  public void testPathWithNodeModules() {
    assertTrue(JSLibraryUtil.isProbableLibraryPath("/var/node_modules/.meteor"));
  }

  public void testPathWithNotNodeModules() {
    assertFalse(JSLibraryUtil.isProbableLibraryPath("/var/node_ms/.meteor"));
  }
}
