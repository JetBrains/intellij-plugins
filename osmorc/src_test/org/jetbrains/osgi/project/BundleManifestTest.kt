/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.jetbrains.osgi.project

import org.junit.Assert
import org.junit.Test

class BundleManifestTest {
  @Test fun bundleSymbolicName() {
    val manifest = BundleManifest(mapOf("Bundle-SymbolicName" to "foo.bar"))
    Assert.assertEquals("foo.bar", manifest.getBundleSymbolicName())
  }

  @Test fun bundleActivator() {
    val manifest = BundleManifest(mapOf("Bundle-Activator" to "foo.bar.WakeMeUp"))
    Assert.assertEquals("foo.bar.WakeMeUp", manifest.getBundleActivator())
  }

  @Test fun exportedPackage() {
    val manifest = BundleManifest(mapOf("Export-Package" to "foo.bar.baz;version= 1.0.0,foo.bar.bam;version= 1.0.0"))
    Assert.assertEquals("foo.bar.baz", manifest.getExportedPackage("foo.bar.baz"))
    Assert.assertEquals("foo.bar.baz", manifest.getExportedPackage("foo.bar.baz.impl"))
    Assert.assertNull(manifest.getExportedPackage("foo.bar.no.way"))
  }

  @Test fun missingHeaderHandling() {
    val manifest = BundleManifest(mapOf())
    Assert.assertNull(manifest.getExportedPackage("pkg"))
  }

  @Test fun importedPackage() {
    val manifest = BundleManifest(mapOf("Import-Package" to "foo.bar.baz;version=\"[1, 2)\""))
    Assert.assertTrue(manifest.isPackageImported("foo.bar.baz"))
    Assert.assertFalse(manifest.isPackageImported("foo.bar.bam"))
    Assert.assertFalse(manifest.isPackageImported("foo.bar"))
  }

  @Test fun requiredBundle() {
    val manifest = BundleManifest(mapOf("Require-Bundle" to "org.apache.felix.framework;bundle-version=\"[0,3)\""))
    Assert.assertTrue(manifest.isBundleRequired("org.apache.felix.framework"))
    Assert.assertFalse(manifest.isBundleRequired("org.apache.felix"))
  }

  @Test fun privatePackage() {
    val manifest = BundleManifest(mapOf("Private-Package" to "org.apache.felix.framework"))
    Assert.assertTrue(manifest.isPrivatePackage("org.apache.felix.framework"))
    Assert.assertTrue(manifest.isPrivatePackage("org.apache.felix.framework.impl"))
    Assert.assertFalse(manifest.isPrivatePackage("org.apache.felix"))
  }
}
