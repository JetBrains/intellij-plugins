package com.intellij.flex.uiDesigner.css {
import flash.errors.IllegalOperationError;
import flash.events.IEventDispatcher;
import flash.system.ApplicationDomain;
import flash.system.SecurityDomain;
import flash.utils.Dictionary;

import mx.core.FlexVersion;
import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.IStyleManager2;

use namespace mx_internal;

[Abstract]
internal class AbstractStyleManager {
  protected var userColorNames:Dictionary;
  private var userParentDisplayListInvalidatingStyles:Dictionary;
  private var userParentSizeInvalidatingStyles:Dictionary;
  private var userSizeInvalidatingStyles:Dictionary;

  private const _selectors:Dictionary = new Dictionary();
  private const _subjects:Dictionary = new Dictionary();
  
  private var pseudoCssStates:Dictionary;
  
  internal var rootStyleDeclarationProxy:StyleDeclarationProxy;
  
  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function set inheritingStyles(value:Object):void {
    throw new IllegalOperationError("forbidden");
  }

  //noinspection JSUnusedGlobalSymbols
  public function get stylesRoot():Object {
    return rootStyleDeclarationProxy;
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function set stylesRoot(value:Object):void {
    throw new IllegalOperationError("unsupported");
  }

  private var _typeSelectorCache:Dictionary;
  //noinspection JSUnusedGlobalSymbols
  public function get typeSelectorCache():Object {
    if (_typeSelectorCache == null) {
      _typeSelectorCache = new Dictionary();
    }
    return _typeSelectorCache;
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function set typeSelectorCache(value:Object):void {
    throw new IllegalOperationError("forbidden");
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function set typeHierarchyCache(value:Object):void {
    throw new IllegalOperationError("forbidden");
  }

  public function getStyleDeclaration(selector:String):CSSStyleDeclaration {
    return _selectors[selector];
  }

  //noinspection JSUnusedGlobalSymbols
  public function getStyleDeclarations(subject:String):Array {    
    var subjects:Array;
    if (parent != null) {
      subjects = parent.getStyleDeclarations(subject);
    }

    if (subjects == null) {
      subjects = _subjects[subject] as Array;
    }
    else {
      var subjectsArray:Array = _subjects[subject] as Array;
      if (subjectsArray != null) {
        subjects = subjects.concat(subjectsArray);
      }
    }

    return subjects;
  }

  /**
   * only Type
   */
  public function registerStyleDeclarationWithOnlyTypeSelector(subject:String, declaration:MergedCssStyleDeclaration):void {
    _selectors[subject] = declaration;
    registerSubject(subject, declaration);
  }
  
  /**
   * only Type.className or .className
   */
  public function registerStyleDeclarationWithOnlyClassCondition(selector:CssSelector, declaration:MergedCssStyleDeclaration):void {
    _selectors[(selector.subject == null ? "." : (selector.subject + ".")) + selector.conditions[0].value] = declaration;
    registerSubject(selector.subject == null ? "*" : selector.subject, declaration);
  }
  
  public function registerStyleDeclarationWithAdvancedSelector(selector:CssSelector, subject:String, declaration:MergedCssStyleDeclaration):void {
    registerSubject(subject == null ? "*" : subject, declaration);
    
    var pseudoCondition:String = selector.getPseudoCondition();
    if (pseudoCondition != null) {
      if (pseudoCssStates == null) {
        pseudoCssStates = new Dictionary();
      }

      pseudoCssStates[pseudoCondition] = true;
    }
    
    _hasAdvancedSelectors = true;
  }
  
  private function registerSubject(subject:String, declaration:MergedCssStyleDeclaration):void {
    declaration.selectorRefCount++;
    
    var declarations:Array = _subjects[subject] as Array;
    if (declarations == null) {
      declarations = [declaration];
      _subjects[subject] = declarations;
    }
    else {
      declarations.push(declaration);
    }
  }

  //noinspection JSUnusedGlobalSymbols
  public function setStyleDeclaration(selector:String, styleDeclaration:CSSStyleDeclaration, update:Boolean):void {
    // see StyleTest#testMxButtonBar41WithLocalStyleHolder
    if (FlexVersion.compatibilityVersion == 0x04000000 && selector == "mx.controls.ButtonBar" && !(styleDeclaration is AbstractCssStyleDeclaration)) {
      return;
    }
    
    _selectors[selector] = styleDeclaration;

    // We also index by subject to help match advanced selectors
    var subject:String = styleDeclaration.subject;

    if (selector != null) {
      if (styleDeclaration.subject == null) {
        // If the styleDeclaration does not yet have a subject we update its selector to keep it in sync with the provided selector.
        styleDeclaration.selectorString = selector;
        subject = styleDeclaration.subject;
      }
      else if (selector != styleDeclaration.selectorString) {
        // The styleDeclaration does not match the provided selector, so we ignore the subject on the styleDeclaration and try to determine the subject from the selector
        var firstChar:String = selector.charAt(0);
        if (firstChar == "." || firstChar == ":" || firstChar == "#") {
          subject = "*";
        }
        else {
          subject = selector;
        }

        // Finally, we update the styleDeclaration's selector to keep
        // it in sync with the provided selector.
        styleDeclaration.selectorString = selector;
      }
    }

    if (subject != null) {
      registerSubject(subject, MergedCssStyleDeclaration(styleDeclaration));
    }
    else {
      styleDeclaration.selectorRefCount++;
    }

    // Also remember subjects that have pseudo-selectors to optimize styles during component state changes.
    var pseudoCondition:String = styleDeclaration.getPseudoCondition();
    if (pseudoCondition != null) {
      if (pseudoCssStates == null) {
        pseudoCssStates = new Dictionary();
      }

      pseudoCssStates[pseudoCondition] = true;
    }

    // Record whether this is an advanced selector so that style declaration look up can be optimized for when no advanced selectors have been declared
    if (styleDeclaration.isAdvanced()) {
      _hasAdvancedSelectors = true;
    }

    // flush cache and start over
    if (_typeSelectorCache != null) {
      _typeSelectorCache = new Dictionary();
    }

    if (update) {
      styleDeclarationsChanged();
    }
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function clearStyleDeclaration(selector:String, update:Boolean):void {
    throw new IllegalOperationError();
  }

  //noinspection JSUnusedGlobalSymbols
  public function isInheritingTextFormatStyle(styleName:String):Boolean {
    return StyleManagerPredefinedData.inheritingTextFormatStyles[styleName] !== undefined;
  }

  //noinspection JSUnusedGlobalSymbols
  public function registerSizeInvalidatingStyle(styleName:String):void {
    if (userSizeInvalidatingStyles == null) {
      userSizeInvalidatingStyles = new Dictionary();
    }

    userSizeInvalidatingStyles[styleName] = true;
  }

  //noinspection JSUnusedGlobalSymbols
  public function isSizeInvalidatingStyle(styleName:String):Boolean {
    return StyleManagerPredefinedData.sizeInvalidatingStyles[styleName] != undefined || (userSizeInvalidatingStyles != null && userSizeInvalidatingStyles[styleName] !== undefined);
  }

  //noinspection JSUnusedGlobalSymbols
  public function registerParentSizeInvalidatingStyle(styleName:String):void {
    if (userParentSizeInvalidatingStyles == null) {
      userParentSizeInvalidatingStyles = new Dictionary();
    }

    userParentSizeInvalidatingStyles[styleName] = true;
  }

  //noinspection JSUnusedGlobalSymbols
  public function isParentSizeInvalidatingStyle(styleName:String):Boolean {
    return StyleManagerPredefinedData.parentDisplayListOrSizeInvalidatingStyles[styleName] != undefined || (userParentSizeInvalidatingStyles != null && userParentSizeInvalidatingStyles[styleName] !== undefined);
  }

  //noinspection JSUnusedGlobalSymbols
  public function registerParentDisplayListInvalidatingStyle(styleName:String):void {
    if (userParentDisplayListInvalidatingStyles == null) {
      userParentDisplayListInvalidatingStyles = new Dictionary();
    }

    userParentDisplayListInvalidatingStyles[styleName] = true;
  }

  //noinspection JSUnusedGlobalSymbols
  public function isParentDisplayListInvalidatingStyle(styleName:String):Boolean {
    return StyleManagerPredefinedData.parentDisplayListOrSizeInvalidatingStyles[styleName] != undefined || (userParentDisplayListInvalidatingStyles != null && userParentDisplayListInvalidatingStyles[styleName] !== undefined);
  }

  //noinspection JSUnusedGlobalSymbols
  public function registerColorName(colorName:String, colorValue:uint):void {
    if (userColorNames == null) {
      userColorNames = new Dictionary();
    }
    userColorNames[colorName.toLowerCase()] = colorValue;
  }

  public function getColorName(colorNameOrValue:Object):uint {
    var colorName:String = colorNameOrValue as String;
    if (colorName == null) {
      return uint(colorNameOrValue);
    }

    var n:Number;
    if (colorName.charAt(0) == "#") {
      // Map "#77EE11" to 0x77EE11
      n = Number("0x" + colorName.slice(1));
      return isNaN(n) ? 0xffffffff : uint(n);
    }

    if (colorName.charAt(1) == "x" && colorName.charAt(0) == '0') {
      n = Number(colorName);
      return isNaN(n) ? 0xffffffff : uint(n);
    }

    var c:*;
    var normalizedColorName:String = colorName.toLowerCase();
    if (userColorNames != null) {
      c = userColorNames[normalizedColorName];
    }
    if (c === undefined) {
      c = StyleManagerPredefinedData.colorNames[normalizedColorName];
      return c === undefined ? (parent != null ? parent.getColorName(colorName) : 0xffffffff) : uint(c);
    }
    else {
      return uint(c);
    }
  }

  //noinspection JSUnusedGlobalSymbols
  public function getColorNames(colors:Array):void {
    if (colors == null) {
      return;
    }

    var n:int = colors.length;
    for (var i:int = 0; i < n; i++) {
      if (colors[i] != null && isNaN(colors[i])) {
        var colorNumber:uint = getColorName(colors[i]);
        if (colorNumber != 0xffffffff) {
          colors[i] = colorNumber;
        }
      }
    }
  }

  //noinspection JSUnusedGlobalSymbols
  public function isValidStyleValue(value:*):Boolean {
    return value !== undefined;
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function loadStyleDeclarations(url:String, update:Boolean = true, trustContent:Boolean = false, applicationDomain:ApplicationDomain = null, securityDomain:SecurityDomain = null):IEventDispatcher {
    throw new IllegalOperationError("unsupported");
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function unloadStyleDeclarations(url:String, update:Boolean = true):void {
    throw new IllegalOperationError("unsupported");
  }

  //noinspection JSUnusedGlobalSymbols
  public function initProtoChainRoots():void {
  }

  public function styleDeclarationsChanged():void {
  }

  public function get parent():IStyleManager2 {
    return null;
  }

  //noinspection JSUnusedGlobalSymbols
  public function get qualifiedTypeSelectors():Boolean {
    return true;
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function set qualifiedTypeSelectors(value:Boolean):void {
    throw new IllegalOperationError("unsupported");
  }

  public function get selectors():Array {
    var allSelectors:Array = [];
    for (var i:String in _selectors) {
      allSelectors.push(i);
    }

    return allSelectors;
  }

  //noinspection JSUnusedGlobalSymbols
  public function hasPseudoCondition(value:String):Boolean {
    if (pseudoCssStates != null && pseudoCssStates[value] != null) {
      return true;
    }

    if (parent) {
      return parent.hasPseudoCondition(value);
    }

    return false;
  }

  private var _hasAdvancedSelectors:Boolean;
  public function hasAdvancedSelectors():Boolean {
    return _hasAdvancedSelectors;
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  public function loadStyleDeclarations2(url:String, update:Boolean = true, applicationDomain:ApplicationDomain = null, securityDomain:SecurityDomain = null):IEventDispatcher {
    throw new IllegalOperationError("unsupported");
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  flex::v4_5
  public function acceptMediaList(value:String):Boolean {
    return false;
  }
}
}