// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import junit.framework.TestCase;

public class IdProviderTest extends TestCase {
  public void testGenerateUniqueId() {
    IdProvider idProvider = new IdProvider();
    String id = idProvider.getId();
    assertNotNull(id);
    assertEquals(id, idProvider.getId());
    assertEquals(32, id.length());

    assertFalse("Id for different projects should differ", id.equals(new IdProvider().getId()));
  }
}
