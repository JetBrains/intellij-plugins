package com.intellij.flex.uiDesigner {
import flash.display.BitmapData;
import flash.display.ColorCorrection;

import org.hamcrest.Matcher;
import org.hamcrest.assertThat;
import org.hamcrest.core.allOf;
import org.hamcrest.core.isA;
import org.hamcrest.core.not;
import org.hamcrest.object.equalTo;
import org.hamcrest.object.instanceOf;
import org.hamcrest.object.nullValue;
import org.hamcrest.object.strictlyEqualTo;

[Test(dir="common")]
public class CommonTest extends BaseTestCase {
  public function SparkComponents():void {
    assertThat(app, {document: app});
  }

  public function SparkApplication():void {
    var m:Object = {colorCorrection: "default"};
    assertThat(app, m);
    app.colorCorrection = ColorCorrection.ON;
    assertThat(app, m);
  }

  public function GenericMxmlSupport():void {

  }

  // AS-110
  public function FxDate():void {
    assertThat(app, [{data: allOf(instanceOf(Date), {month: 8, date: 10, fullYear: 2011})}]);
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
    var tM:Object = {data: strictlyEqualTo(true)};
    var fM:Object = {data: strictlyEqualTo(false)};
    assertThat(app, allOf({left: allOf(isA(int),
                                       strictlyEqualTo(0)), right: strictlyEqualTo("c22:12"), top: strictlyEqualTo(0.4), bottom: allOf(isA(int),
                                                                                                                                       strictlyEqualTo(-30))},
                          [tM, tM, tM, fM, fM, fM, fM, {data: strictlyEqualTo(33)}, {data: strictlyEqualTo(null)}]));
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
    var buttonBarButtonClass:Class = document.module.context.getClass("spark.components.ButtonBarButton");
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
      l("BIG NEW"),
      l("a      https://bugs.adobe.com/jira/browse/SDK-3983           ")
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
      {text: strictlyEqualTo("\n    34\n  "), buttonMode: strictlyEqualTo(true)},
      [l(1), l(2)]
    ]);
  }

  public function InvalidColorName():void {
    assertThat(app, [{color: 0}, {color: 0}, {depth: 0}]);
  }

  public function RuntimeError():void {
    validateUI(); // force update for predictable asserts (updateDisplayList with expected error logs before our tests passed result)
    assertThat(app, [l(1), {} , l(2)]);
  }

  [Test(nullableDocument)]
  public function RuntimeErrorInMxmlRead():void {
    if (document != null) {
      assertThat(document.file.name, not("RuntimeErrorInMxmlRead.mxml"));
    }
  }

  public function CannotFindDefaultProperty():void {
    assertThat(app, [{fill: null}, {text: 2}]);
  }

  public function AbstractClass():void {
    validateUI();
    assertThat(app, [{label: "before invalid control"}, {columns: {source: [{dataField: "foo"}, {dataField: "bar"}]}}]);
  }

  public function ColorEquals0():void {
    assertThat(app, [{color: 0}]);
  }

  public function ProjectActionScriptComponentAsChild():void {
    assertThat(app, [allOf(instanceOf(getClass("spark.components.Button")), {emphasized: true, label: "text"}), instanceOf(getClass("mx.core.UIComponent"))]);
  }

  public function ProjectMxmlComponentAsChild():void {
    assertThat(app, [allOf({name: "IDEA-73453"}, [l("Label in child custom mxml component")])]);
  }

  public function FxVector():void {
    validateUI(); // force list selectedIndices commit
    assertThat(app, [{layout: {constraintColumns: {length: 1, fixed: false}}},
      {selectedIndices: allOf([0, 2], {fixed: false})},
      {selectedIndices: allOf([1, 3], {fixed: false})},
      {selectedIndices: allOf([1, 2], {fixed: false /* false, because our vector copied in spark List */})}
    ]);
  }

  public function IDEA_73613():void {
    assertThat(app, [{color: 0}]);
    setState(B);
    assertThat(app, [{color: 0x008000}]);
  }

  public function FxObject():void {
    validateUI(); // force state specific set

    assertThat(app, [
      {dataProvider: {source: [
        {Artist: "AA", Price: 0.0, Album: "First"}, {Artist: "2423", Price: 0.0, Album: "Second"}
      ]}},
      {dataProvider: {source: [
        {Artist: "AA", Price: 0.0, Album: "First"}, {Artist: "2423", Price: 0.0, Album: "Second"}
      ]}},
      {}
    ]);
  }

  public function FxModel():void {
    var opClass:Class = Class(getDefinition("mx.utils.ObjectProxy"));
    assertThat(app, [
      {data: allOf(instanceOf(opClass),
                   {name: {first: "FN", last: "LN"}, age: strictlyEqualTo(18), h: strictlyEqualTo(13.2), child: allOf(instanceOf(Array), [
                     {name: "Diana"},
                     {name: "Olga"}
                   ]), email: instanceOf(opClass), department: instanceOf(opClass), married: strictlyEqualTo(true)})},
      {},
      {}
    ]);
  }

  public function ProjectMxmlComponentAsParentWithDefaultProperty():void {
    assertThat(app, [l("h")]);
  }

  public function ProjectComponentAsGrandChild():void {
    assertThat(app, [[[l("Label in child custom mxml component")]]]);
  }

  public function PropertyAsTagWithCommentedValueAsTag():void {
    assertThat(app, [{bottom: 1, fill: nullValue()}]);
  }

  public function ItemRendererFunction():void {
    assertThat(app, [[instanceOf(getClass("com.intellij.flex.uiDesigner.flex.UnknownItemRenderer"))]]);
  }
}
}