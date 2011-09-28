package com.intellij.flex.uiDesigner.plugins.test {
import flash.display.BitmapData;
import flash.display.ColorCorrection;

import org.hamcrest.Matcher;
import org.hamcrest.assertThat;
import org.hamcrest.core.allOf;
import org.hamcrest.core.isA;
import org.hamcrest.core.not;
import org.hamcrest.object.equalTo;
import org.hamcrest.object.instanceOf;
import org.hamcrest.object.strictlyEqualTo;
import org.hamcrest.text.emptyString;

public class MxmlTest extends BaseTestCase {
  public function MxmlTest() {
    //noinspection ConstantIfStatementJS
    if (false) {
      SparkApplication();
      SparkComponentsDependOnMx();
      ViewNavigatorApplication();
      SparkComponents();
      MxComponents();
      Embed();
      ResourceDirective();
      UntypedProperty();
      ClassProperty();
      ItemRendererAndMixDefaultExplicitContent();
      WindowedApplication();
      SparkWindow();
      PropertyAsTagWithArrayType();
      RichTextAndCollapseWhitespace();
      MixedTextAndSubTags();
      InlineArrayAsAttributeValue();
      InvalidColorName();
      RuntimeError();
      CannotFindDefaultProperty();
      AbstractClass();
      MouseSelectionTest();
      ColorEquals0();
      ChildrenTypeCheck();
      ProjectActionScriptComponentAsChild();
      ProjectMxmlComponentAsChild();
      EmbedSwfAndImageFromCss();
      EmbedImageAsClass();
      Vector();
      ToolTip();
      MxPanelWithControlBar();
      AreaChartComplexExample();
      ComplexContentAsSubTagsForObjectTypedProperty();
      IDEA_73806();
      IDEA_73613();
    }
  }
  
  public function SparkComponents():void {
    assertThat(app, {document: app});
  }

  public function SparkComponentsDependOnMx():void {
    assertThat(app, {document: app});
  }

  public function MxComponents():void {
    assertThat(app, {document: app});
  }

  public function SparkApplication():void {
    var m:Object = {colorCorrection: ColorCorrection.DEFAULT};
    assertThat(app, m);
    app.colorCorrection = ColorCorrection.ON;
    assertThat(app, m);
  }

  public function ViewNavigatorApplication():void {

  }

  public function Embed():void {
    validateUI(); // force swf get
    var bitmapData:BitmapData = app.getElementAt(0).getElementAt(0).source.data;
    var m:Matcher = allOf(equalTo(bitmapData), {transparent: false, width: 240, height: 180});
    assertThat(app,
               [
                 [
                   {source: {data: m}},
                   {source: {data: m}},
                   {source: {data: {transparent: true, width: 124, height: 44}}},
                   {source: {data: {transparent: true, width: 225, height: 225}}}
                 ],
                 {source: {data: {transparent: true, width: 80, height: 80}}},
                 [
                   {source: {data: {transparent: true, width: 240, height: 180}}},
                   {source: {data: {transparent: true, width: 500, height: 367}}}
                 ],
                 [
                   {},
                   {}
                 ]
               ]);
  }
  
  public function ResourceDirective():void {
    assertThat(app, [{text: "Label"}, {text: ""}]);
  }
  
  public function UntypedProperty():void {
    assertThat(app, {left: allOf(isA(int), strictlyEqualTo(0)), right: strictlyEqualTo("c22:12"), top: strictlyEqualTo(0.4), bottom: allOf(isA(int), strictlyEqualTo(-30))});
  }
  
  public function ClassProperty():void {
    var skinnableContainerSkin:Class = getClass("spark.skins.spark.SkinnableContainerSkin");
    assertThat(app, allOf({skinClass: getClass("spark.skins.spark.ApplicationSkin")}, [
      {skinClass: skinnableContainerSkin},
      {skinClass: skinnableContainerSkin},
      {skinClass: getClass("spark.skins.spark.ButtonBarFirstButtonSkin")},
      {skinClass: getClass("spark.skins.spark.ButtonBarLastButtonSkin")}
    ]));
  }
  
  public function ItemRendererAndMixDefaultExplicitContent():void {
    var buttonBarButtonClass:Class = documentManager.document.module.context.getClass("spark.components.ButtonBarButton");
    var m:Object = {itemRenderer: {generator: buttonBarButtonClass}};
    assertThat(app, [{}, {itemRenderer: {className: "AuxProjectMxmlItemRenderer"}}, m, m]);
  }

  public function WindowedApplication():void {

  }

  public function SparkWindow():void {
    assertThat(app, [{text: "This is a document wffindow."}]);
  }

  public function PropertyAsTagWithArrayType():void {
    assertThat(app.transitions, [{fromState: "*"}, {fromState: "A"}]);
  }
  
  public function RichTextAndCollapseWhitespace():void {
    assertThat(app, [
      {text: "\n      This is paragraph 1.&\n      This is paragraph \"2\".\n    "},
      {text: "dfds        fadfs      f              "},
      {text: "TextArea 1. TextArea 2. TextArea 3."},
      {text: " TextArea as Default Property 1. TextArea as Default Property 2. TextArea as Default Property 3. "},
      {text: " This is paragraph 1. This is paragraph 2. This is paragraph 3. \nThis is paragraph 2."},
      {text: "BIG NEW"},
      {text: "a      https://bugs.adobe.com/jira/browse/SDK-3983           "}
    ]);
    
    assertThat(app.getElementAt(5).textFlow.getElementByID("span1").fontSize, strictlyEqualTo(16));
  }
  
  public function MixedTextAndSubTags():void {
    assertThat(app, [
      {text: "AAA in UUU "},
      {text: "UUU in AAA "},
      {text: "in UUU "},
      {blendMode: "difference", text: "\n\n    UUU\n    "},
      {text: "TRAMUU&"},
      {text: strictlyEqualTo("12"), buttonMode: strictlyEqualTo(true)},
      {text: strictlyEqualTo("\n    34\n    "), buttonMode: strictlyEqualTo(true)},
      {text: strictlyEqualTo("\n    34\n  "), buttonMode: strictlyEqualTo(true)}
    ]);
  }
  
  public function InlineArrayAsAttributeValue():void {
    assertThat(app, [
      {dataProvider: {source: ['Appetizers', 'Entrees', 'Desserts']}},
      {dataProvider: ['one', 'two']}
    ]);
  }
  
  public function InvalidColorName():void {
    assertThat(app, [{color: 0}, {color: 0}, {depth: 0}]);
  }

  public function RuntimeError():void {
    validateUI(); // force update for predictable asserts (updateDisplayList with expected error logs before our tests passed result)
    assertThat(app, [{text: 1}, {} , {text: 2}]);
  }

  [Test(nullableDocument)]
  public function RuntimeErrorInMxmlRead():void {
    if (documentManager.document != null) {
      assertThat(documentManager.document.file.name, not("RuntimeErrorInMxmlRead.mxml"));
    }
  }

  public function CannotFindDefaultProperty():void {
    assertThat(app, [{fill: null}, {text: 2}]);
  }

  public function AbstractClass():void {
    validateUI();
    assertThat(app, [{label: "before invalid control"}, {columns: {source: [{dataField: "foo"}, {dataField: "bar"}]}}]);
  }

  public function MouseSelectionTest():void {
  }

  public function ColorEquals0():void {
    assertThat(app, [{color: 0}]);
  }

  public function ChildrenTypeCheck():void {
    assertThat(app, [[], [], []]);
  }

  public function ProjectActionScriptComponentAsChild():void {
    assertThat(app, []);
  }

  public function ProjectMxmlComponentAsChild():void {
    assertThat(app, [allOf({name: "IDEA-73453"}, [{text: "Label in child custom mxml component"}])]);
  }

  public function EmbedSwfAndImageFromCss():void {
    validateUI();
  }

  public function EmbedImageAsClass():void {
    // todo test over
  }

  public function Vector():void {
    validateUI(); // force list selectedIndices commit
    assertThat(app, [{layout: {constraintColumns: {length: 1, fixed: false}}},
      {selectedIndices: allOf([0, 2], {fixed: false})},
      {selectedIndices: allOf([1, 3], {fixed: false})},
      {selectedIndices: allOf([1, 2], {fixed: false /* false, because our vector copied in spark List*/})}
    ]);
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
      {dataProvider: {list: {source: [strictlyEqualTo("1"), strictlyEqualTo("2")]}}}
    ]);
  }

  public function IDEA_73806():void {
    validateUI(); // force model commit
    assertThat(app, [{dataProvider: {source: instanceOf(XMLList)}}, {text: ""}]);
  }

  public function IDEA_73613():void {
    assertThat(app, [{color: 0}]);
    setState(B);
    assertThat(app, [{color: 0x008000}]);
  }
}
}