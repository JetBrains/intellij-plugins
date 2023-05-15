// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
