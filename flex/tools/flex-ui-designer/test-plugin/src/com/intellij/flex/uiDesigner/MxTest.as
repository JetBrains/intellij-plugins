package com.intellij.flex.uiDesigner {
import org.hamcrest.assertThat;
import org.hamcrest.object.instanceOf;
import org.hamcrest.object.strictlyEqualTo;

[Test(dir="mx")]
public class MxTest extends BaseTestCase {
  public function SparkComponentsDependOnMx():void {
    assertThat(app, {document: app});
  }

  // IDEA-74817
  public function ModuleLoader():void {
  }

  public function MxComponents():void {
    assertThat(app, {document: app});
  }

  public function AssetLoader():void {
  }

  public function InlineArrayAsAttributeValue():void {
    assertThat(app, [
      {dataProvider: {source: ['Appetizers', 'Entrees', 'Desserts']}},
      {dataProvider: ['one', 'two']}
    ]);
  }

  public function MouseSelectionTest():void {
  }

  public function ChildrenTypeCheck():void {
    assertThat(app, [[], [], []]);
  }

  public function EmbedSwfAndImageFromCss():void {
    validateUI();
  }

  public function EmbedImageAsClass():void {
    // todo test over
  }

  public function ToolTip():void {
   
  }

  // IDEA-72935
  public function MxPanelWithControlBar():void {
    assertThat(app, [{width: 250}]);
  }

  public function AreaChartComplexExample():void {
    validateUI();
  }

  // IDEA-73099
  public function ComplexContentAsSubTagsForObjectTypedProperty():void {
    assertThat(app, [
      {dataProvider: {source: [strictlyEqualTo("1"), strictlyEqualTo("2"), strictlyEqualTo("3")]}},
      {dataProvider: {list: {source: [strictlyEqualTo("1"), strictlyEqualTo("2")]}}},
      {dataProvider: [strictlyEqualTo("Button 1"), strictlyEqualTo("Button 2")]},
      {dataProvider: [strictlyEqualTo("Button 1")]}
    ]);
  }

  public function IDEA_73806():void {
    validateUI(); // force model commit
    assertThat(app, [{dataProvider: {source: instanceOf(XMLList)}}, {text: ""}]);
  }
}
}