package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssClassCondition;
import com.intellij.flex.uiDesigner.css.CssPropertyType;
import com.intellij.flex.uiDesigner.css.CssPseudoCondition;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.flex.uiDesigner.libraries.LibrarySetItem;

import flash.text.engine.FontPosture;

import org.hamcrest.*;
import org.hamcrest.collection.arrayWithSize;
import org.hamcrest.core.allOf;
import org.hamcrest.object.instanceOf;
import org.hamcrest.object.notNullValue;
import org.hamcrest.object.nullValue;
import org.hamcrest.object.strictlyEqualTo;

[Test(dir="css")]
public class StyleTest extends BaseTestCase {
  public function emptyForCheckLibrariesCssDefaults():void {
    var librarySets:Vector.<LibrarySet> = documentManager.document.module.librarySets;
    assertThat(librarySets, [{items: arrayWithSize(10)}]);
    assertThat(librarySets, arrayWithSize(1));
    var library:LibrarySetItem;
    for each (var l:LibrarySetItem in librarySets[0].items) {
      if (l.path.indexOf("spark.") != -1) {
        library = l;
        break;
      }
      else if (l.path.indexOf("framework.") == 0) {
        checkFrameworkDefaultsCss(l);
      }
    }
    
    var inherited:Array = "listStylePosition,lineHeight,direction,fontSize,symbolColor,lineThrough,rollOverColor,leadingModel,typographicCase,trackingRight,chromeColor,cffHinting,paragraphEndIndent,unfocusedTextSelectionColor,paragraphSpaceAfter,contentBackgroundAlpha,justificationRule,wordSpacing,textIndent,textShadowAlpha,fontFamily,paragraphSpaceBefore,listAutoPadding,listStyleType,fontStyle,downColor,alternatingItemColors,touchDelay,textAlpha,focusColor,fontWeight,inactiveTextSelectionColor,trackingLeft,whiteSpaceCollapse,alignmentBaseline,kerning,accentColor,dominantBaseline,tabStops,contentBackgroundColor,ligatureLevel,selectionColor,textRotation,textShadowColor,textJustify,digitCase,textDecoration,fontLookup,blockProgression,breakOpportunity,justificationStyle,clearFloats,leading,color,firstBaselineOffset,letterSpacing,paragraphStartIndent,textAlign,baselineShift,digitWidth,textAlignLast,renderingMode,locale,focusedTextSelectionColor,caretColor".split(",");    
    var i:int;
    for (var s:String in library.inheritingStyles) {
      i++;
      if (inherited.indexOf(s) == -1) {
        throw new AssertionError(s + " must be in " + inherited.join(", "));
      }
    }
    assertThat(inherited.length, i);

    var rulesets:Vector.<CssRuleset> = library.defaultsStyle.rulesets;
    assertThat(rulesets.length, 42);
    assertThat(rulesets[0], {selectors: [{subject: "spark.components.Application", conditions: null}], declarations: [{colorName: null, type: CssPropertyType.COLOR_INT, value: 0xffffff}, {name: "skinClass", type: CssPropertyType.CLASS_REFERENCE, value: {className: "spark.skins.spark.ApplicationSkin"}}]});
    assertThat(rulesets[2], {selectors: [{subject: "spark.components.Button", conditions: null, ancestor: null}], declarations: [{colorName: null, name: "skinClass", type: CssPropertyType.CLASS_REFERENCE, value: {className: "spark.skins.spark.ButtonSkin"}}]});
    assertThat(rulesets[3], {selectors: [{subject: "spark.components.Button", conditions: [allOf(instanceOf(CssClassCondition), {value: "emphasized"})]}], declarations: [{colorName: null, name: "skinClass", type: CssPropertyType.CLASS_REFERENCE, value: {className: "spark.skins.spark.DefaultButtonSkin"}}]});
    
    assertThat(rulesets[6], {selectors: [{subject: "spark.components.ComboBox", conditions: null}], declarations: [
      {name: "paddingBottom", type: CssPropertyType.NUMBER, value: 3},
      {name: "paddingLeft", type: CssPropertyType.NUMBER, value: 3},
      {name: "paddingRight", type: CssPropertyType.NUMBER, value: 3},
      {name: "paddingTop", type: CssPropertyType.NUMBER, value: 5},
      {colorName: null, name: "skinClass", type: CssPropertyType.CLASS_REFERENCE, value: {className: "spark.skins.spark.ComboBoxSkin"}}
    ]});
    
    assertThat(rulesets[28], {selectors: [{subject: "spark.components.supportClasses.SkinnableTextBase", conditions: [allOf(instanceOf(CssPseudoCondition), {value: "normalWithPrompt"})], ancestor: null}]});
    assertThat(rulesets[29], {selectors: [{subject: "spark.components.supportClasses.SkinnableTextBase", conditions: [allOf(instanceOf(CssPseudoCondition), {value: "disabledWithPrompt"})], ancestor: null}]});
  }

  private static function checkFrameworkDefaultsCss(library:LibrarySetItem):void {
    var rulesets:Vector.<CssRuleset> = library.defaultsStyle.rulesets;
    assertThat(rulesets.length, 7);
    assertThat(rulesets[0].declarations[59], {name: "highlightAlphas", type: CssPropertyType.ARRAY, value: [0.3, 0]});
    assertThat(rulesets[0].declarations[72], {name: "kerning", type: CssPropertyType.STRING, value: "default"});
  }

  public function StyleTag():void {
    const normalFontSize:int = appContent.getStyle("fontSize");
    //noinspection JSUnusedGlobalSymbols
    assertThat(appContent, [
      {label: "myFontStyle", id: "myButton", styleName: "myFontStyle", fontSize: 15, fontStyle: FontPosture.ITALIC, color: 0x9933FF, backgroundColor: strictlyEqualTo(undefined)},
      {label: "emphasized", emphasized: true, fontSize: normalFontSize, fontStyle: FontPosture.ITALIC, color: 0},
      mouseDown({label: "className and pseudo", styleName: "className", fontSize: normalFontSize, fontStyle: FontPosture.ITALIC, color: 0}, {fontStyle: FontPosture.NORMAL, color: 0xff0000}),
      mouseDown({label: "AAA", styleName: "className", fontSize: normalFontSize, fontStyle: FontPosture.NORMAL, color: 0}, {color: 0xffff00}),
      [
        {label: "ancestor", fontSize: 22, fontStyle: FontPosture.ITALIC, color: 0},
        {label: "disabled (pseudo) ancestor", enabled: false, fontSize: 22, fontStyle: FontPosture.ITALIC, color: 0xffc0cb},
        {label: "pseudo class ancestor", styleName: "aa", fontSize: 22, fontStyle: FontPosture.NORMAL, color: 0x008000},
        allOf({styleName: "bbb"}, [mouseDown({label: "id pseudo class ancestor", id: "ddd", styleName: "aa", fontSize: 28, fontStyle: FontPosture.NORMAL, fontFamily: "_typewriter", fontThickness: 200, color: 0x8b4513}, {color: 0x0000ff})])
      ],
      [{text: "H__________________________|", fontSize: 28, textIndent: -10}],
      {skin: {name: "p"}}
    ]);

    var styleDeclarations:Array = appContent.styleManager.getStyleDeclarations("spark.components.List");
    assertThat(styleDeclarations, arrayWithSize(2));
    assertThat(styleDeclarations[1].getStyle("cornerRadius"), strictlyEqualTo(4));
  }
  
  public function StyleTagWithSource():void {
    assertThat(appContent, [{fontStyle: FontPosture.ITALIC}]);
  }

  public function StyleTagWithSourceAsRelativePath():void {
    // IDEA-72154
    StyleTagWithSource();
  }
  
  public function ComponentWithCustomSkin():void {
    validateUI();
    assertThat(app, [{skin: {minWidth: 21}, labelDisplay: l("My Skinned Button")}]);
  }
  
  public function ComponentWithCustomSkinInPackage():void {
    assertThat(app.skin, {name: "p"});
  }
  
  public function ComponentWithCustomSkinAsBinding():void {
    ComponentWithCustomSkinInPackage();
  }
  
  public function LibraryWithDefaultsCss():void {
    assertThat(app, [{color: 0xff0000}]);
  }

  public function ApplicationLevelGlobalSelector():void {
    assertThat(app, {fontSize: 14, fontStyle: FontPosture.ITALIC});
  }

  public function SeveralStyleSources():void {
    assertThat(app, [{styleName: "myCustomButton", color: 0x000000, icon: notNullValue()}, {styleName: "custom", color: 0xff0000, icon: nullValue()}]);
  }

  public function App1(a:Object = null):void {
    if (a == null) {
      a = app;
    }
    assertThat(a, [{text: "label1", color: 0x000000}]);
  }

  //noinspection JSUnusedGlobalSymbols
  public function App2():void {
    assertThat(app, {color: 0xff0000, fontSize: 18});
    var app1:Object = DocumentFactoryManager(projectManager.project.getComponent(DocumentFactoryManager)).get(0).document.uiComponent;
    assertThat(app1, notNullValue());
    App1(app1);
  }
}
}