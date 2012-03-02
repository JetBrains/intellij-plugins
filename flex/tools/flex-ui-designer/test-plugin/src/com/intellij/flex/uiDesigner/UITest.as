package com.intellij.flex.uiDesigner {
import org.hamcrest.assertThat;

public class UITest extends BaseTestCase {
  public function CloseDocument():void {
    assertThat(document, null);
  }
}
}