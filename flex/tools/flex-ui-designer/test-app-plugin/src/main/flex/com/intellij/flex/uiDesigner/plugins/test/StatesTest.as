package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.StatesBarManager;

import flash.errors.IllegalOperationError;

import org.hamcrest.Matcher;
import org.hamcrest.assertThat;
import org.hamcrest.collection.IsArrayMatcher;
import org.hamcrest.collection.arrayWithSize;
import org.hamcrest.core.allOf;
import org.hamcrest.core.not;
import org.hamcrest.core.throws;
import org.hamcrest.object.HasPropertiesMatcher;
import org.hamcrest.object.equalTo;
import org.hamcrest.object.hasProperties;
import org.hamcrest.object.hasProperty;
import org.hamcrest.object.instanceOf;
import org.hamcrest.object.strictlyEqualTo;
import org.hamcrest.text.containsString;

public class StatesTest extends BaseTestCase {
  private var stateManager:StatesBarManager;
  
  private static const DEFAULT:String = "default";
  
  private static const FIRST_STATE:String = "default";
  private static const SECOND_STATE:String = "Register";
  
  private static const A:String = "A";
  private static const B:String = "B";
  private static const C:String = "C";

  public function StatesTest() {
    // disable unused inspection
    //noinspection ConstantIfStatementJS
    if (false) {
      ChildrenInTheSameState();
      RootChildrenAndSetProperty();
      UnusedStates();
      TwoStatesWithParentByExplicitIdWithoutBackSibling();
      TwoStatesWithParentByExplicitIdWithBackSiblingByExplicitId();
      TwoStatesWithParentWithBackSiblingByExplicitId();
      TwoStatesWithParentWithBackSibling();
      DynamicParent();
      DynamicParentWithBackSibling();
      DynamicParentWithDeepBackSibling();
      DynamicOuterParentWithBackSiblingManyDynamicChildrenInInnerDynamicObject();
      DynamicChildrenLayering();
      ItemDestructionPolicy();
      ItemDestructionPolicyMergeItems();
      ItemDestructionPolicyMergeItems2();
      FirstPendingSetPropertyAndStateGroup();
      SetProperty();
      LoginForm();
      IncludeInAfterStateSpecificProperty();
      IDEA_72004();
      ExcludeFrom();
      ProjectMxmlComponentAsStateSpecificChild();
      ProjectStatefulMxmlComponentAsChild();
      IDEA_73547();
    }
  }

  private function getForm():Object {
    return app.contentGroup.getElementAt(0).contentGroup.getElementAt(0);
  }

  private function assertTwoStates(states:Array):void {
    assertThat(states, [{name: FIRST_STATE}, {name: SECOND_STATE}]);
    assertThat(stateManager.states, [{name: FIRST_STATE}, {name: SECOND_STATE}]);
  }
  
  public function LoginForm():void {

  }
  
  private function setState(name:String):void {
    stateManager.stateName = name;
    validateUI();
  }
  
  private var _states:Array;
  private function get states():Array {
    if (_states == null) {
      _states = documentManager.document.uiComponent.states;
    }
    
    return _states;
  }
  
  override public function setUp():void {
    super.setUp();
    
    stateManager = StatesBarManager(projectManager.project.getComponent(StatesBarManager));
  }
  
  public function RootChildrenAndSetProperty():void {
    var states:Array = app.states;
    assertThat(states, [{name: DEFAULT}, {name: A}, {name: "B"}]);
    
    var matcher:Matcher = hasProperties({label: DEFAULT});
    var matcher2:Matcher = hasProperties({text: "label default"});
    assertThat(app, [matcher, matcher2]);
    
    setState(A);
    assertThat(app, [matcher, {label: "A"}, {text: "label A"}]);
    
    setState(B);
    assertThat(app, [matcher, {label: "B"}, matcher2]);
  }

  public function UnusedStates():void {
    assertThat(app.states, [{name: SECOND_STATE}]);
  }

  public function TwoStatesWithParentByExplicitIdWithoutBackSibling():void {
    var states:Array = app.states;
    assertTwoStates(states);
    
    var form:Object = getForm();
    assertThat(form.numChildren, equalTo(0));
    
    setState(SECOND_STATE);
    assertThat(form, [hasProperties({label: "Password:"})]);
    
    setState(FIRST_STATE);
    validateUI();
    assertThat(form.numChildren, equalTo(0));
  }
  
  private function assertTwoStatesWithForm(formId:String, siblingId:String):void {
    var form:Object = getForm();
    var siblingPropertiesMatcher:Matcher = hasProperties({id: siblingId, label: "Username:"});
    assertThat(form, hasProperties({id: formId}));
    assertThat(form, [siblingPropertiesMatcher]);
    
    setState(SECOND_STATE);
    assertThat(form, [siblingPropertiesMatcher, {label: "Password:"}]);
    
    setState(FIRST_STATE);
    validateUI();
    assertThat(form, [siblingPropertiesMatcher]);
  }
  
  public function TwoStatesWithParentByExplicitIdWithBackSiblingByExplicitId():void {
    var states:Array = documentManager.document.uiComponent.states;
    assertTwoStates(states);

    assertTwoStatesWithForm("loginForm", "username");
  }
  
  public function TwoStatesWithParentWithBackSiblingByExplicitId():void {
    var states:Array = documentManager.document.uiComponent.states;
    assertTwoStates(states);

    assertTwoStatesWithForm(null, "username");
  }
  
  public function TwoStatesWithParentWithBackSibling():void {
    var states:Array = documentManager.document.uiComponent.states;
    assertTwoStates(states);

    assertTwoStatesWithForm(null, null);
  }

  public function DynamicParent():void {
    var form:Object = getForm();
    assertThat(form.numChildren, equalTo(1));
    
    var appContainer:Object = app.contentGroup;
    assertThat(appContainer.numElements, equalTo(1));
    
    setState(A);
    var matcher:Matcher = hasProperties({text: "I am password form item child"});
    assertThat(assertLoginForm(form), [matcher]);
    
    var matcher2:Matcher = hasProperties({text: "all parent states"});
    assertThat(assertVGroup(appContainer), [{text: "only in A"}, matcher2]);
    
    setState(B);
    assertThat(assertLoginForm(form), [{text: "only in B"}, matcher]);
    assertThat(assertVGroup(appContainer), [matcher2]);
  }
  
  private static function assertLoginForm(form:Object):Object {
    assertThat(form.numChildren, equalTo(2));
    assertThat(form.getChildAt(0), {id: "username", label: "Username:"});
    
    var passwordFormItem:Object = form.getChildAt(1);
    assertThat(passwordFormItem, {id: null, label: "Password:"});
    return passwordFormItem;
  }
  
  private static function assertVGroup(appContainer:Object):Object {
    assertThat(appContainer.numChildren, equalTo(2));
    return appContainer.getElementAt(1);
  }
  
  public function ChildrenInTheSameState():void {
  }
  
  public function DynamicParentWithDeepBackSibling():void {
    var container:Object = app.getElementAt(0);
    assertThat(container, []);
    
    setState(A);
    var matcher3:Matcher = hasProperties({id: "inner"});
    var matcher4:Matcher = hasProperties({text: "static"});
    var matcher5:Matcher = hasProperties({id: "outer"});
    assertThat(container, [allOf(matcher5, [allOf(matcher3, [matcher4])])]);
    
    setState(B);
    assertThat(container, [allOf(matcher5, [allOf(matcher3, [matcher4, {text: "dynamic"}])])]);
    
    setState(C);
    assertThat(container, [allOf(matcher5, [])])
  }
  
  public function DynamicParentWithBackSibling():void {
    var container:Object = app.getElementAt(0);

    var matcher:Matcher = hasProperties({id: "username"});
    assertThat(container, [matcher]);
    
    setState(A);
    var m:Object = l(0);
    var m2:Object = {label: "AB"};
    var m3:Object = {label: "Password:"};
    assertThat(container, [matcher, allOf(m3, [allOf({id: "backSibling", toolTip: "a", layout: {gap: 2}}, [])]), allOf(m2, [[m]])]);
    
    setState(B);
    assertThat(container, [matcher, allOf(m3, [allOf({id: "backSibling", toolTip: "d", layout: {gap: 1}}), {text: "i"}]), allOf(m2, [ls(4)])]);
  }
  
  private static function l(i:int):Object {
    return new HasPropertiesMatcher({text: i.toString()});
  }
  
  private static function ls(size:int):Matcher {
    var matchers:Array = new Array(size);
    for (var i:int = 0; i < size; i++) {
      matchers[i] = l(i);
    }
    return new IsArrayMatcher(matchers);
  }

  public function DynamicOuterParentWithBackSiblingManyDynamicChildrenInInnerDynamicObject():void {
    assertThat(app, []);
    
    var m:Object = l(9);
    var m4:Object = l(13);
    // test itemCreationPolicy
    var bDeferredInstances:Array = states[2].overrides[1].itemsFactory.__array;
    //noinspection ReservedWordAsName
    assertThat(bDeferredInstances[0].getInstance, throws(allOf(instanceOf(IllegalOperationError), hasProperty("message", containsString("must be created before this moment")))));

    assertThat(bDeferredInstances[2].getInstance(), m);
    assertThat(states[1].overrides[2].itemsFactory.__array[3].getInstance(), m4);
    
    setState(A);
    var m0:Object = l(0);
    var matcher2:Object = {id: "inner"};
    var matcher3:Object = {id: "outer"};
    var m1:Object = l(4);
    var m2:Object = l(15);
    var matcher4:Array = [allOf(matcher3, [allOf(matcher2, [m0, l(1), l(2), l(3), m1, l(5), l(6), l(12), m4, l(14), m2])])];
    assertThat(app, matcher4);
    
    setState(B);
    assertThat(app, [allOf(matcher3, [allOf(matcher2, [m0, m1, l(7), l(8), m, l(10), l(11), m2])])]);
    
    setState(A);
    assertThat(app, matcher4);
    
    var i:Function = function (size:int):Object {return {items: arrayWithSize(size)};};
    assertThat(states, [{name: DEFAULT, overrides: []}, {name: A, overrides: [i(1), i(3), i(5)]}, {name: B, overrides: [i(1), i(5)]}]);
  }
  
  public function DynamicChildrenLayering():void {
    var m:Object = {destructionPolicy: "never", itemsFactory: {__array: arrayWithSize(2)}};
    assertThat(states[1].overrides, [m, {destructionPolicy: "auto", itemsFactory: {__deferredInstances: arrayWithSize(1)}}, m]);
    assertThat(app, [l(0)]);
    
    setState(A);
    assertThat(app, ls(6));
  }
  
  public function ItemDestructionPolicy():void {    
    setState(A);
    assertThat(app, [l(0)]);
    var old0:Object = app.getElementAt(0);
    setState(DEFAULT);
    assertThat(app, []);
    setState(A);
    assertThat(app.getElementAt(0), not(old0));
  }
  
  public function ItemDestructionPolicyMergeItems():void {
    assertThat(states[1].overrides, [{destructionPolicy: "never", itemsFactory: {__array: arrayWithSize(2)}}, {destructionPolicy: "auto", itemsFactory: {__deferredInstances: arrayWithSize(2)}}]);
    
    setState(A);
    assertThat(app, [l(0), l(1), l(2), l(3)]);
    var old0:Object = app.getElementAt(0);
    var old1:Object = app.getElementAt(1);
    setState(DEFAULT);
    assertThat(app, []);
    setState(A);
    assertThat(app, [not(old0), not(old1), l(2), l(3)]);
  }
  
  public function ItemDestructionPolicyMergeItems2():void {
    var m:Object = {destructionPolicy: "never", itemsFactory: {__array: arrayWithSize(2)}};
    assertThat(states[1].overrides, [m, {destructionPolicy: "auto", itemsFactory: {__deferredInstances: arrayWithSize(2)}}, m]);
  }
  
  public function FirstPendingSetPropertyAndStateGroup():void {
    assertThat(app, []);
    
    setState(A);
    assertThat(app, [{title: "Login", layout: {gap: 2}}]);
    
    setState(B);
    assertThat(app, [{title: "Login", layout: {gap: 3}}]);
    
    setState(C);
    assertThat(app, [{title: "Login", layout: {gap: 4}}]);
  }
  
  public function SetProperty():void {
    assertThat(app, allOf([{text: "mm"}, {text: "uuuuu", percentWidth: strictlyEqualTo(50)}], {layout: {gap: 1}}));
    
    setState(A);
    assertThat(app, allOf([{text: "mm"}, {text: "uuuuu", percentWidth: strictlyEqualTo(100)}], {layout: {gap: 20}}));
  }
  
  public function IncludeInAfterStateSpecificProperty():void {
    assertThat(app, []);
    
    setState(A);
    assertThat(app, [{text: A}]);
    
    setState(B);
    assertThat(app, [{text: B}]);
  }

  public function IDEA_72004():void {
    validateUI();
    assertThat(app, [{width: 40}]);
  }

  public function ExcludeFrom():void {
    validateUI();
    assertThat(app, [{text: "U"}]);
    setState(A);
    assertThat(app, []);
  }

  public function ProjectMxmlComponentAsStateSpecificChild():void {
    assertThat(app, []);
    setState(A);
    assertThat(app, [[{text: "Label in child custom mxml component"}]]);
  }

  public function ProjectStatefulMxmlComponentAsChild():void {
    validateUI();
    assertThat(app, [[[[[{text: "static"}]]]]]);
    setState(A);
    assertThat(app, [[[[[{text: "static"}, {text: "dynamic"}]]]]]);
    //assertThat(app, [[{text: "Label in child custom mxml component"}]]);
  }

  public function IDEA_73547():void {
  }

  public function IDEA_73550():void {
  }
}
}