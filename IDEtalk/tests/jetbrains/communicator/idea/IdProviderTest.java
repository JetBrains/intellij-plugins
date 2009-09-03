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

import junit.framework.TestCase;

/**
 * @author Kir
 */
public class IdProviderTest extends TestCase {
  public void testGenerateUniqueId() throws Exception {
    IdProvider idProvider = new IdProvider(null);
    String id = idProvider.getId();
    assertNotNull(id);
    assertEquals(id, idProvider.getId());
    assertEquals(32, id.length());

    assertFalse("Id for different projects should differ", id.equals(new IdProvider(null).getId()));
  }
}
