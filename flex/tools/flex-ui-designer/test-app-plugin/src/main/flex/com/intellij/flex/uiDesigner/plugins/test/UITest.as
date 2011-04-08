package com.intellij.flex.uiDesigner.plugins.test {
import org.hamcrest.assertThat;

public class UITest extends BaseTestCase {
  public function UITest() {
    // disable unused inspection
    //noinspection ConstantIfStatementJS
    if (false) {
      CloseDocument();
    }
  }
  
  [Test(nullableDocument)]
  public function CloseDocument():void {
    assertThat(documentManager.document, null);
  }
}
}