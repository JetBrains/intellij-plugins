package mx.styles {
import avmplus.HIDE_OBJECT;
import avmplus.INCLUDE_BASES;
import avmplus.INCLUDE_TRAITS;
import avmplus.describe;

import com.intellij.flex.uiDesigner.css.AbstractCssStyleDeclaration;
import com.intellij.flex.uiDesigner.css.InlineCssRuleset;
import com.intellij.flex.uiDesigner.css.InlineCssStyleDeclaration;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.util.ImmutableFakeObjectProxy;

import flash.errors.IllegalOperationError;
import flash.utils.getQualifiedClassName;

import mx.core.IFlexModule;
import mx.core.IInvalidating;
import mx.core.IUITextField;
import mx.core.mx_internal;
import mx.effects.EffectManager;
import mx.utils.NameUtil;

use namespace mx_internal;

public class StyleProtoChain {
  // debug only
  //FtyleProtoChain.STYLE_UNINITIALIZED.wtf = true;

  public static const STYLE_UNINITIALIZED:Object = FtyleProtoChain.STYLE_UNINITIALIZED;

  public static function getClassStyleDeclarations(object:IStyleClient):Array {
    var styleManager:IStyleManager2 = getStyleManager(object);
    const qualified:Boolean = styleManager.qualifiedTypeSelectors;
    const className:String = qualified ? getClassName(object) : object.className;
    var classDecls:Array;

    const hasAdvancedSelectors:Boolean = styleManager.hasAdvancedSelectors();
    if (!hasAdvancedSelectors && (classDecls = styleManager.typeSelectorCache[className]) != null) {
      return classDecls;
    }

    classDecls = [];

    var advancedObject:IAdvancedStyleClient = object as IAdvancedStyleClient;
    var typeHierarchy:TypeHierarchyCacheItem = getTypeHierarchy(object, styleManager, qualified);
    var types:Vector.<String> = typeHierarchy.chain;
    var typeCount:int = types.length;
    // Loop over the type hierarchy starting at the base type and work down the chain of subclasses.
    for (var i:int = typeCount - 1; i >= 0; i--) {
      var type:String = types[i];
      if (hasAdvancedSelectors && advancedObject != null) {
        var decls:Object = styleManager.getStyleDeclarations(type);
        if (decls != null) {
          classDecls = classDecls.concat(FtyleProtoChain.matchStyleDeclarations(decls, advancedObject));
        }
      }
      else {
        var decl:CSSStyleDeclaration = styleManager.getMergedStyleDeclaration(type);
        if (decl != null) {
          classDecls.push(decl);
        }
      }
    }

    if (hasAdvancedSelectors && advancedObject != null) {
      // Advanced selectors may result in more than one match per type so
      // we sort based on specificity, but we preserve the declaration order for equal selectors.
      return FtyleProtoChain.sortOnSpecificity(classDecls);
    }
    else {
      // Cache the simple type declarations for this class
      styleManager.typeSelectorCache[className] = classDecls;
      return classDecls;
    }
  }

  //noinspection JSUnusedGlobalSymbols
  public static function initProtoChain(object:IStyleClient, inheritPopUpStylesFromOwner:Boolean = true):void {
    if (object.styleDeclaration != null && !(object.styleDeclaration is AbstractCssStyleDeclaration)) {
      object.styleDeclaration = new InlineCssStyleDeclaration(InlineCssRuleset.createExternalInlineWithFactory(object.styleDeclaration.defaultFactory, false), StyleManagerEx(getStyleManager(object)).styleValueResolver);
    }

    FtyleProtoChain.initProtoChain(object);

    if (!isValidStyleHolder(object.inheritingStyles) || !isValidStyleHolder(object.nonInheritingStyles)) {
      throw new IllegalOperationError("Internal error while init proto chain");
    }
  }

  private static function isValidStyleHolder(o:Object):Boolean {
    return o is ImmutableFakeObjectProxy;
  }

  public static function initProtoChainForUIComponentStyleName(obj:IStyleClient):void {
    FtyleProtoChain.initProtoChainForUIComponentStyleName(obj);
  }

  public static function initTextField(obj:IUITextField):void {
    FtyleProtoChain.initTextField(obj);
  }

  public static function styleChanged(object:IInvalidating, styleProp:String):void {
    FtyleProtoChain.styleChanged(object, styleProp);
  }

  public static function matchesCSSType(object:IAdvancedStyleClient, cssType:String):Boolean {
    var styleManager:IStyleManager2 = getStyleManager(object);
    return getTypeHierarchy(object, styleManager, styleManager.qualifiedTypeSelectors)[cssType];
  }

  public static function getMatchingStyleDeclarations(object:IAdvancedStyleClient, styleDeclarations:Array = null):Array {
    return FtyleProtoChain.getMatchingStyleDeclarations(object, styleDeclarations);
  }

  public static function setStyle(object:IStyleClient, styleProp:String, newValue:*):void {
    if (styleProp == "styleName") {
      object.styleName = newValue;
      return;
    }
    
    EffectManager.setStyle(styleProp, object);
    
    var styleManager:IStyleManager2 = getStyleManager(object);
    const isProtoChainInitialized:Boolean = object.inheritingStyles != STYLE_UNINITIALIZED;
    const callStyleChangedAfter:Boolean = isProtoChainInitialized && object.getStyle(styleProp) != newValue;
    if (object.styleDeclaration == null) {
      object.styleDeclaration = new InlineCssStyleDeclaration(InlineCssRuleset.createRuntime(styleProp, newValue), StyleManagerEx(styleManager).styleValueResolver);
      if (isProtoChainInitialized) {
        object.regenerateStyleCache(styleManager.isInheritingStyle(styleProp));
      }
    }
    else {
      InlineCssStyleDeclaration(object.styleDeclaration).ruleset.put(styleProp, newValue);
    }

    if (callStyleChangedAfter) {
      object.styleChanged(styleProp);
      object.notifyStyleChangeInChildren(styleProp, styleManager.isInheritingStyle(styleProp));
    }
  }

  private static function getStyleManager(object:Object):IStyleManager2 {
    if (object is IFlexModule) {
      return StyleManager.getStyleManager(IFlexModule(object).moduleFactory);
    }
    else if (object is StyleProxy) {
      return getStyleManagerFromStyleProxy(StyleProxy(object));
    }
    else {
      throw new ArgumentError("what is " + object);
    }
  }

  private static function getStyleManagerFromStyleProxy(object:StyleProxy):IStyleManager2 {
    var curObj:IStyleClient = object;
    while (curObj is StyleProxy) {
      curObj = StyleProxy(curObj).source;
    }

    return getStyleManager(curObj);
  }

  // our impl doesn't require ApplicationDomain
  private static function getTypeHierarchy(object:IStyleClient, styleManager:IStyleManager2, qualified:Boolean = true):TypeHierarchyCacheItem {
    var className:String = getClassName(object);
    var hierarchy:TypeHierarchyCacheItem = TypeHierarchyCacheItem(styleManager.typeHierarchyCache[className]);
    if (hierarchy != null) {
      return hierarchy;
    }

    hierarchy = new TypeHierarchyCacheItem();

    if (isStopClass(className)) {
      hierarchy.chain = new Vector.<String>(0, true);
      styleManager.typeHierarchyCache[normalizeClassName(className, qualified)] = hierarchy;
    }
    else {
      var bases:Array = describe(object, INCLUDE_BASES | HIDE_OBJECT | INCLUDE_TRAITS).traits.bases;
      className = normalizeClassName(className, qualified);
      styleManager.typeHierarchyCache[className] = hierarchy;

      hierarchy[className] = true;
      if (bases.length == 1 /* last element always is Object */ || isStopClass(bases[0])) {
        hierarchy.chain = new <String>[className];
      }
      else {
        var n:int = bases.length - 1;
        var chain:Vector.<String> = new Vector.<String>(n + 1);
        chain[0] = className;
        var s:int = 1;
        for (var i:int = 0; i < n; i++) {
          className = bases[i];
          //if (className == "spark.components.supportClasses::FkinnableComponent" || className == "mx.controls::FWFLoader") {
          //  continue;
          //}

          if (!isStopClass(className)) {
            className = normalizeClassName(className, qualified);
            chain[s++] = className;
            hierarchy[className] = true;
          }
          else {
            break;
          }
        }

        chain.length = s;
        chain.fixed = true;
        hierarchy.chain = chain;
      }
    }

    return hierarchy;
  }

  private static function getClassName(object:IStyleClient):String {
    var className:String = getQualifiedClassName(object);
    //if (className == "spark.components.supportClasses::FkinnableComponent") {
    //  return "spark.components.supportClasses::SkinnableComponent";
    //}
    //else if (className == "mx.controls::FWFLoader") {
    //  return "mx.controls::SWFLoader";
    //}
    //else {
      return className;
    //}
  }

  private static function normalizeClassName(className:String, qualified:Boolean):String {
    return qualified ? className.replace("::", ".") : NameUtil.getUnqualifiedClassName(className);
  }

  private static function isStopClass(value:String):Boolean {
    return value == "mx.core::UIComponent" ||
           value == "mx.core::UITextField" ||
           value == "mx.graphics.baseClasses::GraphicElement";
  }
}
}

import flash.utils.Dictionary;

final dynamic class TypeHierarchyCacheItem extends Dictionary {
  public var chain:Vector.<String>;
}