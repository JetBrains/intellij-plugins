package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.libraries.LibrarySetItem;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.flex.uiDesigner.libraries.Library;
import com.intellij.flex.uiDesigner.css.CssClassCondition;
import com.intellij.flex.uiDesigner.css.CssPropertyType;
import com.intellij.flex.uiDesigner.css.CssPseudoCondition;
import com.intellij.flex.uiDesigner.css.CssRuleset;

import flash.text.engine.FontPosture;

import org.hamcrest.*;
import org.hamcrest.collection.arrayWithSize;
import org.hamcrest.core.allOf;
import org.hamcrest.object.instanceOf;
import org.hamcrest.object.strictlyEqualTo;

public class StyleTest extends BaseTestCase {
  public function StyleTest() {
    //noinspection ConstantIfStatementJS
    if (false) {
      empty();
      StyleTag();
      StyleTagWithSource();
      ComponentWithCustomSkin();
      ComponentWithCustomSkinInPackage();
      ComponentWithCustomSkinAsBinding();
      LibraryWithDefaultsCss();
    }
  }

  public function empty():void {
    var librarySets:Vector.<LibrarySet> = documentManager.document.module.librarySets;
    assertThat(librarySets, [{libraries: arrayWithSize(11)}]);
    assertThat(librarySets, arrayWithSize(1));
    var library:Library;
    for each (var l:LibrarySetItem in librarySets[0].items) {
      if (l is Library && l.path.indexOf("spark.") != -1) {
        library = Library(l);
        break;
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

    var style:Vector.<CssRuleset> = library.defaultsStyle.rulesets;
    assertThat(style.length, 42);
    assertThat(style[0], {selectors: [{subject: "spark.components.Application", conditions: null}], declarations: [{colorName: null, type: CssPropertyType.COLOR_INT, value: 0xffffff}, {name: "skinClass", type: CssPropertyType.CLASS_REFERENCE, value: {className: "spark.skins.spark.ApplicationSkin"}}]});
    assertThat(style[2], {selectors: [{subject: "spark.components.Button", conditions: null, ancestor: null}], declarations: [{colorName: null, name: "skinClass", type: CssPropertyType.CLASS_REFERENCE, value: {className: "spark.skins.spark.ButtonSkin"}}]});
    assertThat(style[3], {selectors: [{subject: "spark.components.Button", conditions: [allOf(instanceOf(CssClassCondition), {value: "emphasized"})]}], declarations: [{colorName: null, name: "skinClass", type: CssPropertyType.CLASS_REFERENCE, value: {className: "spark.skins.spark.DefaultButtonSkin"}}]});
    
    assertThat(style[6], {selectors: [{subject: "spark.components.ComboBox", conditions: null}], declarations: [
      {name: "dropShadowVisible", type: CssPropertyType.BOOL, value: strictlyEqualTo(true)},
      {name: "paddingBottom", type: CssPropertyType.NUMBER, value: 3},
      {name: "paddingLeft", type: CssPropertyType.NUMBER, value: 3},
      {name: "paddingRight", type: CssPropertyType.NUMBER, value: 3},
      {name: "paddingTop", type: CssPropertyType.NUMBER, value: 5},
      {colorName: null, name: "skinClass", type: CssPropertyType.CLASS_REFERENCE, value: {className: "spark.skins.spark.ComboBoxSkin"}}
    ]});
    
    assertThat(style[28], {selectors: [{subject: "spark.components.supportClasses.SkinnableTextBase", conditions: [allOf(instanceOf(CssPseudoCondition), {value: "normalWithPrompt"})], ancestor: null}]});
    assertThat(style[29], {selectors: [{subject: "spark.components.supportClasses.SkinnableTextBase", conditions: [allOf(instanceOf(CssPseudoCondition), {value: "disabledWithPrompt"})], ancestor: null}]});
  }

  public function StyleTag():void {
    const normalFontSize:int = appContent.getStyle("fontSize");
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
      ]
    ]);
  }
  
  public function StyleTagWithSource():void {
    assertThat(appContent, [{fontStyle: FontPosture.ITALIC}]);
  }
  
  public function ComponentWithCustomSkin():void {
    assertThat(app.skin, [{fill: {color: 0x3366ff}}, {id: "contentGroup", left: -181}]);
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
}
}