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
package org.jetbrains.osgi.highlighting

import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.osmorc.LightOsgiFixtureTestCase
import org.osmorc.i18n.OsmorcBundle
import org.osmorc.inspection.UnregisteredActivatorInspection

class UnregisteredActivatorInspectionTest : LightOsgiFixtureTestCase() {
  fun testUnregistered() {
    doTest("""
        package pkg;
        import org.osgi.framework.*;
        public class <error descr="Bundle activator is not registered in the manifest">C</error> implements BundleActivator {
          public void start(BundleContext context) throws Exception { }
          public void stop(BundleContext context) throws Exception { }
        }""")
  }

  fun testRegistered() {
    doTest("""
        package pkg;
        import org.osgi.framework.*;
        public class C implements BundleActivator {
          public void start(BundleContext context) throws Exception { }
          public void stop(BundleContext context) throws Exception { }
        }""",
        "Bundle-Activator: pkg.C\n")
  }

  fun testRegisteredBnd() {
    myFixture.addFileToProject("bnd.bnd", "Bundle-Activator: pkg.C")
    myConfiguration.manifestGenerationMode = ManifestGenerationMode.Bnd
    doTest("""
        package pkg;
        import org.osgi.framework.*;
        public class C implements BundleActivator {
          public void start(BundleContext context) throws Exception { }
          public void stop(BundleContext context) throws Exception { }
        }""")
  }

  fun testAbstract() {
    doTest("""
        package pkg;
        import org.osgi.framework.*;
        public abstract class C implements BundleActivator { }""")
  }

  fun testGroovy() {
    doTest("""
        package pkg
        import org.osgi.framework.*
        class <error descr="Bundle activator is not registered in the manifest">C</error> implements BundleActivator {
          void start(BundleContext context) { }
          void stop(BundleContext context) { }
        }""", fileName = "C.groovy")
  }

  fun testQuickFix() {
    doTestFix(
        "Bundle-Activator: pkg.X\n",
        "Bundle-Activator: pkg.C\n")
  }

  private fun doTest(classText: String, manifestText: String = "", fileName: String = "C.java") {
    myFixture.enableInspections(UnregisteredActivatorInspection())
    myFixture.addFileToProject("META-INF/MANIFEST.MF", manifestText)
    myFixture.configureByText(fileName, classText)
    myFixture.checkHighlighting()
  }

  private fun doTestFix(text: String, expected: String) {
    myFixture.enableInspections(UnregisteredActivatorInspection())
    val manifest = myFixture.addFileToProject("META-INF/MANIFEST.MF", text)
    myFixture.configureByText("C.java", """
        package pkg;
        import org.osgi.framework.*;
        public class <caret>C implements BundleActivator {
          public void start(BundleContext context) throws Exception { }
          public void stop(BundleContext context) throws Exception { }
        }""")
    val intention = myFixture.findSingleIntention(OsmorcBundle.message("UnregisteredActivatorInspection.fix.manifest"))
    myFixture.launchAction(intention)
    assertEquals(expected, manifest.text)
  }
}