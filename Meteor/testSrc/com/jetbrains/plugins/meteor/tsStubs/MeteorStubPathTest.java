package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.util.containers.ContainerUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class MeteorStubPathTest {

  @Test
  public void testVersionNumber() {
    List<MeteorStubPath.VersionNumber> versions =
      ContainerUtil.sorted(List.of(create("meteor-v1.1.1.d.ts"), create("meteor-v0.1.d.ts"), create("meteor-v0.2.2.d.ts"), create("meteor-v0.2.d.ts")));

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

  @Test
  public void testPathWithNodeModules() {
    assertTrue(JSLibraryUtil.isProbableLibraryPath("/var/node_modules/.meteor"));
  }

  @Test
  public void testPathWithNotNodeModules() {
    assertFalse(JSLibraryUtil.isProbableLibraryPath("/var/node_ms/.meteor"));
  }
}
