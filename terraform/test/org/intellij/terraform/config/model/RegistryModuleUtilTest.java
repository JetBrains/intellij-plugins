/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
package org.intellij.terraform.config.model;

import junit.framework.TestCase;

public class RegistryModuleUtilTest extends TestCase {
  public void testParseRegistryModule() {
    doTestParseRegistryModuleOk("namespace/id/provider", null, "namespace/id/provider");
    doTestParseRegistryModuleOk("registry.com/namespace/id/provider", "registry.com", "namespace/id/provider");
    doTestParseRegistryModuleOk("registry.com:4443/namespace/id/provider", "registry.com:4443", "namespace/id/provider");
    doTestParseRegistryModuleOk("namespace/id/provider//subdir", null, "namespace/id/provider");
    doTestParseRegistryModuleOk("registry.com/ns/id/provider//subdir", "registry.com", "ns/id/provider");
    doTestParseRegistryModuleOk("registry.com/namespace/id/provider", "registry.com", "namespace/id/provider");

    doTestParseRegistryModuleFail("git::https://example.com/vpc.git");
    doTestParseRegistryModuleFail("git::ssh://username@example.com/storage.git");
    doTestParseRegistryModuleFail("registry.com/namespace/id/provider/extra");
    doTestParseRegistryModuleFail("./local/file/path");
    doTestParseRegistryModuleFail("./registry.com/namespace/id/provider");
    doTestParseRegistryModuleFail("https://example.com/foo/bar/baz");
    doTestParseRegistryModuleFail("xn--80akhbyknj4f.com/namespace/id/provider");
    doTestParseRegistryModuleFail("github.com/namespace/id/provider");
    doTestParseRegistryModuleFail("git@github.com:namespace/id/provider");
    doTestParseRegistryModuleFail("bitbucket.org/namespace/id/provider");
  }


  private void doTestParseRegistryModuleFail(String source) {
    RegistryModuleUtil.RegistryModule module = RegistryModuleUtil.INSTANCE.parseRegistryModule(source);
    assertNull(module);
  }

  private void doTestParseRegistryModuleOk(String source, String host, String id) {
    RegistryModuleUtil.RegistryModule module = RegistryModuleUtil.INSTANCE.parseRegistryModule(source);
    assertNotNull(module);
    assertEquals(host, module.getHost());
    String computedId = module.getNamespace() + '/' + module.getName() + '/' + module.getProvider();
    assertEquals(id, computedId);
  }
}
