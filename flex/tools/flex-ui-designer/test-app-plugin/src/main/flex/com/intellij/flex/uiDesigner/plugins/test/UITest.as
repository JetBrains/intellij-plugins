package com.intellij.flex.uiDesigner.plugins.test {
import org.hamcrest.assertThat;

public class UITest extends BaseTestCase {
  [Test(nullableDocument)]
  public function CloseDocument():void {
    assertThat(documentManager.document, null);
  }
}
}