package com.intellij.flex.uiDesigner {
import org.hamcrest.assertThat;
import org.hamcrest.object.notNullValue;

[Test(dir="mobile")]
public class MobileTest extends BaseTestCase {
  public function ViewNavigatorApplication():void {
    validateUI();
    assertThat(app, {navigator: {length: 1, activeView: notNullValue()}});
  }

  public function SparkView():void {
  }
}
}
