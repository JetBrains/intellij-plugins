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

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,JSMethodCanBeStatic
  public function set inheritingStyles(value:Object):void {
    throw new IllegalOperationError("forbidden");
  }

  //noinspection JSUnusedGlobalSymbols
  public function get stylesRoot():Object {
    return rootStyleDeclarationProxy;
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,JSMethodCanBeStatic
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

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,JSMethodCanBeStatic
  public function set typeSelectorCache(value:Object):void {
    throw new IllegalOperationError("forbidden");
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,JSMethodCanBeStatic
  public function set typeHierarchyCache(value:Object):void {
    throw new IllegalOperationError("forbidden");
  }

  public function getStyleDeclaration(selector:String):CSSStyleDeclaration {
    return _selectors[selector];
  }

  flex::v4_6
  public function getStyleDeclarations(subject:String):Object {
    var declarations:Array = _getStyleDeclarations(subject);
    var burnInHellAdobe:BurnInHellAdobe = new BurnInHellAdobe();
    for each (var declaration:CssStyleDeclaration in declarations) {
      var selector:CssSelector = declaration._selector;
      var firstCondition:CssCondition = selector == null || selector.conditions == null || selector.conditions.length == 0 ?
                                        null
              : selector.conditions[0];
      if (firstCondition == null) {
        var unconditional:Array = burnInHellAdobe.unconditional;
        if (unconditional == null) {
          unconditional = [declaration];
          burnInHellAdobe.unconditional = unconditional;
        }
        else {
          unconditional[unconditional.length] = declaration;
        }
      }
      else if (firstCondition is CssClassCondition) {
        var clazz:Array = burnInHellAdobe["class"];
        if (clazz == null) {
          clazz = [declaration];
          burnInHellAdobe["class"] = clazz;
        }
        else {
          clazz[clazz.length] = declaration;
        }
      }
      else if (firstCondition is CssPseudoCondition) {
        var pseudo:Array = burnInHellAdobe.pseudo;
        if (pseudo == null) {
          pseudo = [declaration];
          burnInHellAdobe.pseudo = pseudo;
        }
        else {
          pseudo[pseudo.length] = declaration;
        }
      }
      else if (firstCondition is CssIdCondition) {
        var id:Array = burnInHellAdobe.id;
        if (id == null) {
          id = [declaration];
          burnInHellAdobe.id = id;
        }
        else {
          id[id.length] = declaration;
        }
      }
    }

    return burnInHellAdobe;
  }

  flex::lt_4_6
  public function getStyleDeclarations(subject:String):Array {
    return _getStyleDeclarations(subject);
  }

  private function _getStyleDeclarations(subject:String):Array {
    var subjects:Array;
    var p:AbstractStyleManager = parent as RootStyleManager;
    if (p != null) {
      subjects = p._getStyleDeclarations(subject) as Array;
    }

    if (subjects == null) {
      return _subjects[subject] as Array;
    }
    else {
      var subjectsArray:Array = _subjects[subject] as Array;
      if (subjectsArray != null) {
        subjects = subjects.concat(subjectsArray);
      }
      return subjects;
    }
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
  
  private function registerSubject(subject:String, declaration:AbstractCssStyleDeclaration):void {
    declaration.selectorRefCount++;
    
    var declarations:Array = _subjects[subject] as Array;
    if (declarations == null) {
      _subjects[subject] = [declaration];
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

    if (selector == null) {
      selector = styleDeclaration.selectorString;
    }

    var subject:String;
    var conditions:Vector.<CssCondition>;
    var firstChar:String = selector.charAt(0);
    if (firstChar == "." || firstChar == ":" || firstChar == "#") {
      subject = "*";
      conditions = new <CssCondition>[new CssClassCondition(selector.substring(1))];
    }
    else if (firstChar == ":" || firstChar == "#") {
      throw new IllegalOperationError("unsuported");
    }
    else {
      subject = selector;
    }

    var declaration:RuntimeCssStyleDeclaration = new RuntimeCssStyleDeclaration(new CssSelector(subject, subject, null, conditions, null), styleDeclaration, StyleManagerEx(this).styleValueResolver);
    _selectors[selector] = declaration;
    registerSubject(subject, declaration);

    // flush cache and start over
    if (_typeSelectorCache != null) {
      _typeSelectorCache = new Dictionary();
    }

    if (update) {
      styleDeclarationsChanged();
    }
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,JSMethodCanBeStatic
  public function clearStyleDeclaration(selector:String, update:Boolean):void {
    throw new IllegalOperationError();
  }

  //noinspection JSUnusedGlobalSymbols,JSMethodCanBeStatic
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

  //noinspection JSUnusedGlobalSymbols,JSMethodCanBeStatic
  public function isValidStyleValue(value:*):Boolean {
    return value !== undefined;
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,JSMethodCanBeStatic
  public function loadStyleDeclarations(url:String, update:Boolean = true, trustContent:Boolean = false, applicationDomain:ApplicationDomain = null, securityDomain:SecurityDomain = null):IEventDispatcher {
    throw new IllegalOperationError("unsupported");
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,JSMethodCanBeStatic
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

  //noinspection JSUnusedGlobalSymbols,JSMethodCanBeStatic
  public function get qualifiedTypeSelectors():Boolean {
    return true;
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,JSMethodCanBeStatic
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

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,JSMethodCanBeStatic
  public function loadStyleDeclarations2(url:String, update:Boolean = true, applicationDomain:ApplicationDomain = null, securityDomain:SecurityDomain = null):IEventDispatcher {
    throw new IllegalOperationError("unsupported");
  }

  //noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,JSMethodCanBeStatic
  flex::gt_4_1
  public function acceptMediaList(value:String):Boolean {
    return false;
  }
}
}

dynamic class BurnInHellAdobe {
  public var pseudo:Array;
  public var id:Array;
  public var unconditional:Array;
}