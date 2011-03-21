package com.intellij.flex.uiDesigner.plugins.test {
import org.hamcrest.assertThat;
import org.hamcrest.core.allOf;
import org.hamcrest.core.isA;
import org.hamcrest.object.strictlyEqualTo;

public class MxmlTest extends BaseTestCase {
  public function MxmlTest() {
    //noinspection ConstantIfStatementJS
    if (false) {
      Form();
      Embed();
      UntypedProperty();
      ClassProperty();
      ItemRendererAndMixDefaultExplicitContent();
      WindowedApplication();
      PropertyAsTagWithArrayType();
      RichTextAndCollapseWhitespace();
      MixedTextAndSubTags();
      InlineArrayAsAttributeValue();
    }
  }
  
  public function Form():void {
    assertThat(app, {document: app});
  }
  
  public function Embed():void {
    assertThat(app, {document: app});
  }
  
  public function UntypedProperty():void {
    assertThat(app, {left: allOf(isA(int), strictlyEqualTo(0)), right: strictlyEqualTo("c22:12"), top: strictlyEqualTo(0.4), bottom: allOf(isA(int), strictlyEqualTo(-30))});
  }
  
  public function ClassProperty():void {
    assertThat(app, {skinClass: getClass("spark.skins.spark.ApplicationSkin")});
  }
  
  public function ItemRendererAndMixDefaultExplicitContent():void {

  }
  
  public function WindowedApplication():void {

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
   // http://youtrack.jetbrains.net/issue/IDEA-64721 
  }
}
}
