package com.intellij.flex.uiDesigner.plugins.test {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.IEventDispatcher;
import flash.events.MouseEvent;
import flash.utils.Dictionary;

import mx.managers.LayoutManager;

import org.hamcrest.Matcher;
import org.hamcrest.assertThat;
import org.hamcrest.core.allOf;
import org.hamcrest.core.not;
import org.hamcrest.object.QualifiedName;
import org.hamcrest.object.equalTo;
import org.hamcrest.object.hasProperties;

public class InjectedASTest extends BaseTestCase {
  public function InjectedASTest() {
    // disable unused inspection
    //noinspection ConstantIfStatementJS
    if (false) {
      ArrayLiteralOfReferences();
      Transitions();
      PopUpAnchor();
      StateSpecificBinding();
      ExplicitRadioButtonGroup();
      OneElementAsArray();
      TwoWayBinding();
      ArrayOfPrimitives();
      UnresolvedVariableInScriptAsArrayItem();
      Constructor();
      Model();
      BindingToDeferredInstanceFromBytes();
    }
  }
  
  private static function mouseDown(target:Object):void {
    IEventDispatcher(target).dispatchEvent(new MouseEvent(MouseEvent.MOUSE_DOWN));
  }
  
  public function Transitions():void {
    assertThat(app.explicitWidth, 400);
    assertThat(app.transitions, [{effect: {targets: [{title: "One"}, {title: "Two"}, {title: "Three"}]}}]);
  }

  [Test(async)]
  public function ArrayLiteralOfReferences():void {
    var effectManagerClass:Class = getClass("mx.effects.EffectManager");
    assertThat(effectManagerClass[getMxInternal("createEffectForType")](app, MouseEvent.MOUSE_DOWN), {alphaFrom: 1, repeatBehavior: "reverse", targets: [app]});
    assertThat(getClass("mx.core.FlexGlobals").topLevelApplication, app);
    
    // 9
    LayoutManager.getInstance().validateNow();
    
    var view:Sprite = Sprite(app);
    var oldH:Number = view.height;
    mouseDown(view);
    view.addEventListener("effectEnd", function (event:Event):void {
      assertThat(view.height, oldH);

      asyncSuccess(event, arguments.callee);
    });
  }
  
  public function PopUpAnchor():void {
    // todo
  }
  
  public function StateSpecificBinding():void {
    // todo
  }
  
  private function getMxInternal(propertyName:String):QName {
    return new QName(mxInternal, propertyName);
  }
  
  public function ExplicitRadioButtonGroup():void {
    var group:Object = app.getElementAt(0).getElementAt(0).group;
    var mxInternalName:QName = getMxInternal("name");
    var p:Dictionary = new Dictionary();
    p[new QualifiedName(mxInternalName)] = allOf(equalTo(group[mxInternalName]), not("radioGroup"));
    var groupNameMatcher:Matcher = allOf(equalTo(group), hasProperties(p)); 
    assertThat(app, [[{id: "americanExpress", group: groupNameMatcher}, {id: "masterCard", group: groupNameMatcher}, {id: "visa", group: groupNameMatcher}, {id: "myTA"}]]);
  }
  
//  [Test(async)]
  public function OneElementAsArray():void {
    
    // todo support mouseDown="r.play()"
//    LayoutManager.getInstance().validateNow();
//    
//    mouseDown(app.getElementAt(0));
  }
  
  public function TwoWayBinding():void {
    // todo
  }
  
  public function ArrayOfPrimitives():void {
    var m:Object = {dataProvider: null};
    var m2:Object = {source: ['IntelliJ IDEA', 'Flex IDE', 'ReSharper', 'YouTrack', 'TeamCity']};
    assertThat(app, [
      {dataProvider: m2, selectionColor: 0xffffffff, rollOverColor: 0xffffffff},
      {dataProvider: {source: ["Item 1"]}},
      m,
      {dataProvider: m2}
    ]);
  }

  public function UnresolvedVariableInScriptAsArrayItem():void {
    
  }

  public function Constructor():void {
  }

  public function Model():void {
  }

  public function BindingToDeferredInstanceFromBytes():void {
    validateUI();
  }
}
}
