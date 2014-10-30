package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.testFramework.LightPlatformTestCase;

public class DartDocumentationProviderTest extends LightPlatformTestCase {

  public void testSdkPrefixes() throws Exception {
    assertLibPrefixEquals("dart.core", "http://api.dartlang.org/docs/releases/latest/dart_core");
    assertLibPrefixEquals("dart.io", "http://api.dartlang.org/docs/releases/latest/dart_io");
    assertLibPrefixEquals("dart.math", "http://api.dartlang.org/docs/releases/latest/dart_math");
  }

  public void testDomLibPrefixes() throws Exception {
    assertLibPrefixEquals("dart.dom.html", "http://api.dartlang.org/docs/releases/latest/dart_html");
    assertLibPrefixEquals("dart.dom.svg", "http://api.dartlang.org/docs/releases/latest/dart_svg");
    assertLibPrefixEquals("dart.webgl", "http://api.dartlang.org/docs/releases/latest/dart_webgl");
  }

  public void testApiDocHostedLibPrefixes() throws Exception {
    assertLibPrefixEquals("barback", "http://api.dartlang.org/docs/releases/latest/barback");
    assertLibPrefixEquals("unittest", "http://api.dartlang.org/docs/releases/latest/unittest");
  }

  private void assertLibPrefixEquals(final String libName, final String expectedPrefix) {
    assertEquals(expectedPrefix, DartDocumentationProvider.constructDocUrlPrefix(libName).toString());
  }

}
