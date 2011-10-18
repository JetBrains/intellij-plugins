package com.intellij.flex.uiDesigner {
import org.hamcrest.assertThat;

public class UITest extends BaseTestCase {
  [Test(nullableDocument)]
  public function CloseDocument():void {
    assertThat(documentManager.document, null);
  }
}
}