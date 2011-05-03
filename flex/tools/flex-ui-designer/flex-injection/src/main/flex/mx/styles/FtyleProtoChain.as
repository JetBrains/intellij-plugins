////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2004-2007 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

package mx.styles
{

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.system.ApplicationDomain;
import flash.utils.getQualifiedClassName;
import flash.utils.getQualifiedSuperclassName;

import mx.core.FlexGlobals;
import mx.core.IFlexDisplayObject;
import mx.core.IFlexModule;
import mx.core.IFlexModuleFactory;
import mx.core.IFontContextComponent;
import mx.core.IInvalidating;
import mx.core.IUITextField;
import mx.core.IVisualElement;
import mx.core.UIComponent;
import mx.core.mx_internal;
import mx.effects.EffectManager;
import mx.managers.SystemManager;
import mx.modules.IModule;
import mx.modules.ModuleManager;
import mx.utils.NameUtil;
import mx.utils.OrderedObject;
import mx.utils.object_proxy;

use namespace mx_internal;
use namespace object_proxy;

[ExcludeClass]

/**
 *  @private
 *  This is an all-static class with methods for building the protochains
 *  that Flex uses to look up CSS style properties.
 */
public class StyleProtoChain
{
    include "../core/Version.as";

    //--------------------------------------------------------------------------
    //
    //  Class constants
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  The inheritingStyles and nonInheritingStyles properties
     *  are initialized to this empty Object.
     *  This allows the getStyle() and getStyle()
     *  methods to simply access inheritingStyles[] and nonInheritingStyles[]
     *  without needing to first check whether those objects exist.
     *  If they were simply initialized to {}, we couldn't determine
     *  whether the style chain has already been built or not.
     */
    public static var STYLE_UNINITIALIZED:Object = {};

    //--------------------------------------------------------------------------
    //
    //  Class methods
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  Implements the getClassStyleDeclarations() logic
     *  for UIComponent and TextBase.
     *  The 'object' parameter will be one or the other.
     */
    public static function getClassStyleDeclarations(object:IStyleClient):Array
    {
        var styleManager:IStyleManager2 = getStyleManager(object);
        var qualified:Boolean = styleManager.qualifiedTypeSelectors;
        var className:String = qualified ? getQualifiedClassName(object) : object.className;
        var advancedObject:IAdvancedStyleClient = object as IAdvancedStyleClient;

        var typeHierarchy:OrderedObject = getTypeHierarchy(object, qualified);
        var types:Array = typeHierarchy.propertyList;
        var typeCount:int = types.length;
        var classDecls:Array = null;

        if (!styleManager.hasAdvancedSelectors())
        {
            classDecls = styleManager.typeSelectorCache[className];
            if (classDecls)
                return classDecls;
        }

        classDecls = [];

        // Loop over the type hierarhcy starting at the base type and work
        // down the chain of subclasses.
        for (var i:int = typeCount - 1; i >= 0; i--)
        {
            var type:String = types[i].toString();
            if (styleManager.hasAdvancedSelectors() && advancedObject != null)
            {
                var decls:Array = styleManager.getStyleDeclarations(type);
                if (decls)
                {
                    var matchingDecls:Array = matchStyleDeclarations(decls, advancedObject);
                    classDecls = classDecls.concat(matchingDecls);
                }
            }
            else
            {
                var decl:CSSStyleDeclaration = styleManager.getMergedStyleDeclaration(type);
                if (decl)
                    classDecls.push(decl);
            }
        }

        if (styleManager.hasAdvancedSelectors() && advancedObject != null)
        {        
            // Advanced selectors may result in more than one match per type so
            // we sort based on specificity, but we preserve the declaration
            // order for equal selectors.
            classDecls = sortOnSpecificity(classDecls);
        }
        else
        {
            // Cache the simple type declarations for this class 
            styleManager.typeSelectorCache[className] = classDecls;
        }

        return classDecls;
    }

    /**
     *  @private
     *  Implements the initProtoChain() logic for UIComponent and TextBase.
     *  The 'object' parameter will be one or the other.
     */
    public static function initProtoChain(object:IStyleClient):void
    {
        var styleManager:IStyleManager2 = getStyleManager(object);
        var n:int;
        var i:int;

        var uicObject:UIComponent = object as UIComponent;
        var advancedObject:IAdvancedStyleClient = object as IAdvancedStyleClient;
        var styleDeclaration:CSSStyleDeclaration = null;

        var universalSelectors:Array = [];
        var hasStyleName:Boolean = false;
        var styleName:Object = object.styleName;
        if (styleName)
        {
            if (styleName is CSSStyleDeclaration)
            {
                // Get the styles referenced by the styleName property.
                universalSelectors.push(CSSStyleDeclaration(styleName));
            }
            else if (styleName is IFlexDisplayObject || styleName is IStyleClient)
            {
                // If the styleName property is a UIComponent, then there's a
                // special search path for that case.
                StyleProtoChain.initProtoChainForUIComponentStyleName(object);
                return;
            }
            else if (styleName is String)
            {
                hasStyleName = true;
            }
        }

        // To build the proto chain, we start at the end and work forward.
        // Referring to the list at the top of this function, we'll start
        // by getting the tail of the proto chain, which is:
        //  - for non-inheriting styles, the global style sheet
        //  - for inheriting styles, my parent's style object
        var nonInheritChain:Object = styleManager.stylesRoot;

        if (nonInheritChain && nonInheritChain.effects)
            object.registerEffects(nonInheritChain.effects);

        var p:IStyleClient = null;
        if (object is IVisualElement)
            p = IVisualElement(object).parent as IStyleClient;
        else if (object is IAdvancedStyleClient) 
            p = IAdvancedStyleClient(object).styleParent as IStyleClient;
			
        if (p)
        {
            var inheritChain:Object = p.inheritingStyles;
            if (inheritChain == StyleProtoChain.STYLE_UNINITIALIZED)
                inheritChain = nonInheritChain;

            // If this object is a module then add its global styles to the 
            // inheritChain. If we don't have global styles in this style manager
            // then the user didn't declare a global style in the module and the
            // compiler didn't add a duplicate default style. In that case don't 
            // add global styles to the chain because the parent style manager's
            // global styles are already on the chain.
            if (object is IModule)
            {
                styleDeclaration = styleManager.getStyleDeclaration("global");
                if (styleDeclaration)
                    inheritChain = styleDeclaration.addStyleToProtoChain(inheritChain, DisplayObject(object));
            }
        }
        else
        {
            // Pop ups inheriting chain starts at Application instead of global.
            // This allows popups to grab styles like themeColor that are
            // set on Application.
            if (uicObject && uicObject.isPopUp)
            {
                var owner:DisplayObjectContainer = uicObject._owner;
                if (owner && owner is IStyleClient)
                {
                    inheritChain = IStyleClient(owner).inheritingStyles;
                }
                else
                {
                    inheritChain = FlexGlobals.topLevelApplication.inheritingStyles;
                }
            }
            else
            {
                inheritChain = styleManager.stylesRoot;
            }
        }

        var styleDeclarations:Array = null;

        // If we have an advanced style client, we handle this separately
        // because of the considerably more complex selector matches...
        if (styleManager.hasAdvancedSelectors() && advancedObject != null)
        {
            styleDeclarations = getMatchingStyleDeclarations(advancedObject, universalSelectors);

            n = styleDeclarations != null ? styleDeclarations.length : 0;
            for (i = 0; i < n; i++)
            {
                styleDeclaration = styleDeclarations[i];
                inheritChain = styleDeclaration.addStyleToProtoChain(inheritChain, uicObject);
                nonInheritChain = styleDeclaration.addStyleToProtoChain(nonInheritChain, uicObject);

                if (styleDeclaration.effects)
                    advancedObject.registerEffects(styleDeclaration.effects);
            }
        }
        // Otherwise we use the legacy Flex 3 logic for simple selectors.
        else
        {
            // Get the styles referenced by the styleName property
            if (hasStyleName)
            {             
                var styleNames:Array = styleName.split(/\s+/);
                n = styleNames.length;
                for (i = 0; i < n; i++)
                {
                    if (styleNames[i].length)
                    {
                        styleDeclaration = styleManager.getMergedStyleDeclaration("." + styleNames[i]);
                        if (styleDeclaration)
                            universalSelectors.push(styleDeclaration);
                    }
                }
            }

            // Working backwards up the list, the next element in the
            // search path is the type selector
            styleDeclarations = object.getClassStyleDeclarations();
            n = styleDeclarations != null ? styleDeclarations.length : 0;
            for (i = 0; i < n; i++)
            {
                styleDeclaration = styleDeclarations[i];
                inheritChain = styleDeclaration.addStyleToProtoChain(inheritChain, uicObject);
                nonInheritChain = styleDeclaration.addStyleToProtoChain(nonInheritChain, uicObject);

                if (styleDeclaration.effects)
                    object.registerEffects(styleDeclaration.effects);
            }

            // Next are the class selectors
            n = universalSelectors.length;
            for (i = 0; i < n; i++)
            {
                styleDeclaration = universalSelectors[i];
                if (styleDeclaration)
                {
                    inheritChain =
                        styleDeclaration.addStyleToProtoChain(inheritChain, uicObject);
    
                    nonInheritChain =
                        styleDeclaration.addStyleToProtoChain(nonInheritChain, uicObject);
    
                    if (styleDeclaration.effects)
                        object.registerEffects(styleDeclaration.effects);
                }
            }
        }

        // Finally, we'll add the in-line styles
        // to the head of the proto chain.
        
        styleDeclaration = object.styleDeclaration;

        object.inheritingStyles =
            styleDeclaration ?
            styleDeclaration.addStyleToProtoChain(inheritChain, uicObject) :
            inheritChain;

        object.nonInheritingStyles =
            styleDeclaration ?
            styleDeclaration.addStyleToProtoChain(nonInheritChain, uicObject) :
            nonInheritChain;
    }

    /**
     *  @private
     *  If the styleName property points to a UIComponent, then we search
     *  for stylable properties in the following order:
     *  
     *  1) Look for inline styles on this object
     *  2) Look for inline styles on the styleName object
     *  3) Look for class selectors on the styleName object
     *  4) Look for type selectors on the styleName object
     *  5) Look for type selectors on this object
     *  6) Follow the usual search path for the styleName object
     *  
     *  If this object doesn't have any type selectors, then the
     *  search path can be simplified to two steps:
     *  
     *  1) Look for inline styles on this object
     *  2) Follow the usual search path for the styleName object
     */
    public static function initProtoChainForUIComponentStyleName(
                                    obj:IStyleClient):void
    {
        var styleManager:IStyleManager2 = getStyleManager(obj);
        var styleName:IStyleClient = IStyleClient(obj.styleName);
        var target:DisplayObject = obj as DisplayObject;
        
        // Push items onto the proto chain in reverse order, beginning with
        // 6) Follow the usual search path for the styleName object
        var nonInheritChain:Object = styleName.nonInheritingStyles;
        if (!nonInheritChain ||
            nonInheritChain == StyleProtoChain.STYLE_UNINITIALIZED)
        {
            nonInheritChain = styleManager.stylesRoot;

            if (nonInheritChain.effects)
                obj.registerEffects(nonInheritChain.effects);
        }

        var inheritChain:Object = styleName.inheritingStyles;
        if (!inheritChain ||
            inheritChain == StyleProtoChain.STYLE_UNINITIALIZED)
        {
            inheritChain = styleManager.stylesRoot;
        }

        // If there's no type selector on this object, then we can collapse
        // 6 steps to 2 (see above)
        var typeSelectors:Array = obj.getClassStyleDeclarations();
        var n:int = typeSelectors.length;
        
        // If we are a StyleProxy and we aren't building the protochain from
        // our type selectors, then we need to build the protochain from
        // the styleName since styleName.nonInheritingStyles is always null.
        if (styleName is StyleProxy)
        {   
            if (n == 0)
            {   
                // 4) Look for type selectors on the styleName object
                // 3) Look for class selectors on the styleName object
                // 2) Look for inline styles on the styleName object
                nonInheritChain = addProperties(nonInheritChain, styleName, false);
            }
            target = StyleProxy(styleName).source as DisplayObject;
        }
        
        for (var i:int = 0; i < n; i++)
        {
            var typeSelector:CSSStyleDeclaration = typeSelectors[i];

            // If there's no *inheriting* type selector on this object, then we
            // can still collapse 6 steps to 2 for the inheriting properties.

            // 5) Look for type selectors on this object
            inheritChain = typeSelector.addStyleToProtoChain(inheritChain, target); 

            // 4) Look for type selectors on the styleName object
            // 3) Look for class selectors on the styleName object
            // 2) Look for inline styles on the styleName object
            inheritChain = addProperties(inheritChain, styleName, true);

            // 5) Look for type selectors on this object
            nonInheritChain = typeSelector.addStyleToProtoChain(nonInheritChain, target);   

            // 4) Look for type selectors on the styleName object
            // 3) Look for class selectors on the styleName object
            // 2) Look for inline styles on the styleName object
            nonInheritChain = addProperties(nonInheritChain, styleName, false);

            if (typeSelector.effects)
                obj.registerEffects(typeSelector.effects);
        }
        
        // 1) Look for inline styles on this object
        
        obj.inheritingStyles =
            obj.styleDeclaration ? 
            obj.styleDeclaration.addStyleToProtoChain(inheritChain, target) :
            inheritChain;
        
        obj.nonInheritingStyles =
            obj.styleDeclaration ? 
            obj.styleDeclaration.addStyleToProtoChain(nonInheritChain, target) :
            nonInheritChain;
    }
    
    /**
     *  See the comment for the initProtoChainForUIComponentStyleName
     *  function. The comment for that function includes a six-step
     *  sequence. This sub-function implements the following pieces
     *  of that sequence:
     *  
     *  2) Look for inline styles on the styleName object
     *  3) Look for class selectors on the styleName object
     *  4) Look for type selectors on the styleName object
     *  
     *   This piece is broken out as a separate function so that it
     *  can be called recursively when the styleName object has a
     *  styleName property is itself another UIComponent.
     *  
     *  @langversion 3.0
     *  @playerversion Flash 9
     *  @playerversion AIR 1.1
     *  @productversion Flex 3
     */
    private static function addProperties(chain:Object, obj:IStyleClient,
                                          bInheriting:Boolean):Object
    {
        // Only use a filter map if styleName is a StyleProxy and we are building the nonInheritingStyles chain
        var filterMap:Object = obj is StyleProxy && !bInheriting ? StyleProxy(obj).filterMap : null;
        
        // StyleProxy's usually have sources that are DisplayObject's, but a StyleProxy can also have 
        // another StyleProxy as it's source (Example: CalendarLayout's source is a StyleProxy for DateChooser, 
        // whose style is a StyleProxy for DateField)
        
        // The way we use target is a bit hacky, but we always assume that styles (if pointed to DisplayObjects)
        // are the parent (or atleast an ancestor), and we rely on this down the line (such as in 
        // DataGridColumn.addStyleToProtoChain)
        var curObj:IStyleClient = obj;
        while (curObj is StyleProxy)
        {
            curObj = StyleProxy(curObj).source;
        }
        var target:DisplayObject = curObj as DisplayObject;

        var advancedObject:IAdvancedStyleClient = obj as IAdvancedStyleClient;
        var styleName:Object = obj.styleName;
        var styleDeclarations:Array;
        var decl:CSSStyleDeclaration;
        var styleManager:IStyleManager2 = getStyleManager(target);
        
        // If we have an advanced style client, we handle this separately
        // because of the considerably more complex selector matches...
        if (advancedObject != null && styleManager.hasAdvancedSelectors())
        {
            // Handle special case of styleName as a CSSStyleDeclaration
            if (styleName is CSSStyleDeclaration)
            {
                styleDeclarations = [CSSStyleDeclaration(styleName)];
            }

            // Find matching style declarations, sorted by specificity
            styleDeclarations = getMatchingStyleDeclarations(advancedObject, styleDeclarations);

            // Then apply matching selectors to the proto chain
            for (i = 0; i < styleDeclarations.length; i++)
            {
                decl = styleDeclarations[i];
                if (decl)
                {
                    chain = decl.addStyleToProtoChain(chain, target, filterMap);
                    if (decl.effects)
                        obj.registerEffects(decl.effects);
                }
            }

            // Finally, handle special case of styleName as an IStyleClient
            // which overrides any of the selectors above
            if (styleName is IStyleClient)
            {
                // If the styleName property is another UIComponent, then
                // recursively add type selectors, class selectors, and
                // inline styles for that UIComponent
                chain = addProperties(chain, IStyleClient(styleName),
                                      bInheriting);
            }
        }
        else
        {
            // 4) Add type selectors 
            styleDeclarations = obj.getClassStyleDeclarations();
            var n:int = styleDeclarations.length;
            for (var i:int = 0; i < n; i++)
            {
                decl = styleDeclarations[i];
                chain = decl.addStyleToProtoChain(chain, target, filterMap);
    
                if (decl.effects)
                    obj.registerEffects(decl.effects);
            }

            // 3) Add class selectors
            if (styleName)
            {
                styleDeclarations = [];
                if (typeof(styleName) == "object")
                {
                    if (styleName is CSSStyleDeclaration)
                    {
                        // Get the style sheet referenced by the styleName property.
                        styleDeclarations.push(CSSStyleDeclaration(styleName));
                    }
                    else
                    {               
                        // If the styleName property is another UIComponent, then
                        // recursively add type selectors, class selectors, and
                        // inline styles for that UIComponent
                        chain = addProperties(chain, IStyleClient(styleName),
                                              bInheriting);
                    }
                }
                else
                {
                    // Get the style sheets referenced by the styleName property             
                    var styleNames:Array = styleName.split(/\s+/);
                    for (var c:int=0; c < styleNames.length; c++)
                    {
                        if (styleNames[c].length)
                        {
                            styleDeclarations.push(styleManager.getMergedStyleDeclaration("." + styleNames[c]));
                        }
                    }
                }

                for (i = 0; i < styleDeclarations.length; i++)
                {
                    decl = styleDeclarations[i];
                    if (decl)
                    {
                        chain = decl.addStyleToProtoChain(chain, target, filterMap);
                        if (decl.effects)
                            obj.registerEffects(decl.effects);
                    }
                }
            }
        }

        // 2) Add inline styles 
        if (obj.styleDeclaration)
            chain = obj.styleDeclaration.addStyleToProtoChain(chain, target, filterMap);

        return chain;
    }

    /**
     *  @private
     */
    public static function initTextField(obj:IUITextField):void
    {
        // TextFields never have any inline styles or type selector, so
        // this is an optimized version of the initObject function (above)
        var styleManager:IStyleManager2 = StyleManager.getStyleManager(obj.moduleFactory);
        var styleName:Object = obj.styleName;
        var classSelectors:Array = [];
        
        if (styleName)
        {
            if (typeof(styleName) == "object")
            {
                if (styleName is CSSStyleDeclaration)
                {
                    // Get the style sheet referenced by the styleName property.
                    classSelectors.push(CSSStyleDeclaration(styleName));
                }
                else if (styleName is StyleProxy)
                {
                    obj.inheritingStyles =
                        IStyleClient(styleName).inheritingStyles;
                        
                    obj.nonInheritingStyles = addProperties(styleManager.stylesRoot, IStyleClient(styleName), false);
                    
                    return;
                }
                else
                {               
                    // styleName points to a UIComponent, so just set
                    // this TextField's proto chains to be the same
                    // as that UIComponent's proto chains.          
                    obj.inheritingStyles =
                        IStyleClient(styleName).inheritingStyles;
                    obj.nonInheritingStyles =
                        IStyleClient(styleName).nonInheritingStyles;
                    return;
                }
            }
            else
            {                   
                // Get the style sheets referenced by the styleName property             
                var styleNames:Array = styleName.split(/\s+/);
                for (var c:int=0; c < styleNames.length; c++)
                {
                    if (styleNames[c].length) {
                        classSelectors.push(styleManager.getMergedStyleDeclaration("." + 
                            styleNames[c]));
                    }
                }    
            }
        }
        
        // To build the proto chain, we start at the end and work forward.
        // We'll start by getting the tail of the proto chain, which is:
        //  - for non-inheriting styles, the global style sheet
        //  - for inheriting styles, my parent's style object
        var inheritChain:Object = IStyleClient(obj.parent).inheritingStyles;
        var nonInheritChain:Object = styleManager.stylesRoot;
        if (!inheritChain)
            inheritChain = styleManager.stylesRoot;
                
        // Next are the class selectors
        for (var i:int = 0; i < classSelectors.length; i++)
        {
            var classSelector:CSSStyleDeclaration = classSelectors[i];
            if (classSelector)
            {
                inheritChain =
                    classSelector.addStyleToProtoChain(inheritChain, DisplayObject(obj));

                nonInheritChain =
                    classSelector.addStyleToProtoChain(nonInheritChain, DisplayObject(obj));
            }
        }
        
        obj.inheritingStyles = inheritChain;
        obj.nonInheritingStyles = nonInheritChain;
    }

    /**
     *  @private
     *  Implements the setStyle() logic for UIComponent and TextBase.
     *  The 'object' parameter will be one or the other.
     */
    public static function setStyle(object:IStyleClient, styleProp:String,
                                    newValue:*):void
    {
        var styleManager:IStyleManager2 = getStyleManager(object);
        
        if (styleProp == "styleName")
        {
            // Let the setter handle this one, see UIComponent.
            object.styleName = newValue;

            // Short circuit, because styleName isn't really a style.
            return;
        }

        if (EffectManager.getEventForEffectTrigger(styleProp) != "")
            EffectManager.setStyle(styleProp, object);

        // If this object didn't previously have any inline styles,
        // then regenerate its proto chain
        // (and the proto chains of its descendants).
        var isInheritingStyle:Boolean =
            styleManager.isInheritingStyle(styleProp);
        var isProtoChainInitialized:Boolean =
            object.inheritingStyles != StyleProtoChain.STYLE_UNINITIALIZED;
        var valueChanged:Boolean = object.getStyle(styleProp) != newValue;
        
        if (!object.styleDeclaration)
        {
            object.styleDeclaration = new CSSStyleDeclaration(null, styleManager);
           
            object.styleDeclaration.setLocalStyle(styleProp, newValue);

            // If inheritingStyles is undefined, then this object is being
            // initialized and we haven't yet generated the proto chain.  To
            // avoid redundant work, don't bother to create the proto chain here.
            if (isProtoChainInitialized)
                object.regenerateStyleCache(isInheritingStyle);
        }
        else
        {
            object.styleDeclaration.setLocalStyle(styleProp, newValue);
        }

        if (isProtoChainInitialized && valueChanged)
        {
            object.styleChanged(styleProp);
            object.notifyStyleChangeInChildren(styleProp, isInheritingStyle);
        }
    }

    /**
     *  @private
     *  Implements the styleChanged() logic for UIComponent and TextBase.
     *  The 'object' parameter will be one or the other.
     */
    public static function styleChanged(object:IInvalidating, styleProp:String):void
    {
        var styleManager:IStyleManager2 = getStyleManager(object);
       
        // If font changed, then invalidateProperties so
        // we can re-create the text field in commitProperties
        // TODO (gosmith): Should hasFontContextChanged() be added to IFontContextComponent?
        if (object is IFontContextComponent &&
            "hasFontContextChanged" in object &&
            object["hasFontContextChanged"]())
        {
            object.invalidateProperties();
        }
        
        if (!styleProp || 
            styleProp == "styleName" ||
            styleProp == "layoutDirection")
        {
            object.invalidateProperties();
        }        
        
        // Check to see if this is one of the style properties
        // that is known to affect layout.
        if (!styleProp ||
            styleProp == "styleName" ||
            styleManager.isSizeInvalidatingStyle(styleProp))
        {
            // This style property change may affect the layout of this
            // object. Signal the LayoutManager to re-measure the object.
            object.invalidateSize();
        }

        // TODO (gosmith): Should initThemeColor() be in some interface?
        if (!styleProp || 
            styleProp == "styleName" ||
            styleProp == "themeColor")
        {
        	if (object is UIComponent)
                object["initThemeColor"]();
        }
        
        object.invalidateDisplayList();
        
        var parent:IInvalidating;
        if (object is IVisualElement)
            parent = IVisualElement(object).parent as IInvalidating;

        if (parent)
        {
            if (styleProp == "styleName" || styleManager.isParentSizeInvalidatingStyle(styleProp))
                parent.invalidateSize();

            if (styleProp == "styleName" || styleManager.isParentDisplayListInvalidatingStyle(styleProp))
                parent.invalidateDisplayList();
        }
    }

    /**
     *  @private
     */
    public static function matchesCSSType(object:IAdvancedStyleClient, cssType:String):Boolean
    {
        var styleManager:IStyleManager2 = getStyleManager(object);
        var qualified:Boolean = styleManager.qualifiedTypeSelectors;
        var typeHierarchy:OrderedObject = getTypeHierarchy(object, qualified);
        return typeHierarchy.object_proxy::getObjectProperty(cssType) != null;
    }

    /**
     *  @private  
     *  Find all matching style declarations for an IAdvancedStyleClient
     *  component. The result is sorted in terms of specificity, but the
     *  declaration order is preserved.
     *
     *  @param object - an IAdvancedStyleClient instance of the component to
     *  match.
     *  @param styleDeclarations - an optional Array of additional
     *  CSSStyleDeclarations to be included in the sorted matches.
     *
     *  @return An Array of matching style declarations sorted by specificity.
     */
    public static function getMatchingStyleDeclarations(object:IAdvancedStyleClient,
            styleDeclarations:Array=null):Array // of CSSStyleDeclaration
    {
        var styleManager:IStyleManager2 = getStyleManager(object);
        
        if (styleDeclarations == null)
            styleDeclarations = [];

        // First, look for universal selectors
        var universalDecls:Array = styleManager.getStyleDeclarations("*");
        styleDeclarations = matchStyleDeclarations(universalDecls, object).concat(styleDeclarations);

        // Next, look for type selectors (includes ActionScript supertype matches)
        // If we also had universal selectors, concatenate them with our type
        // selectors and then resort by specificity...
        if (styleDeclarations.length > 0)
        {
            styleDeclarations = object.getClassStyleDeclarations().concat(styleDeclarations);
            styleDeclarations = sortOnSpecificity(styleDeclarations);
        }
        else
        {
            // Otherwise, we only have type selectors (which are already sorted)
            styleDeclarations = object.getClassStyleDeclarations();
        }
        return styleDeclarations;
    }

    /**
     *  @private
     *  @param object - the IStyleClient to be introspected  
     *  @param qualified - whether qualified type names should be used
     *  @return an ordered map of class names, starting with the object's class
     *  name and then each super class name until we hit a stop class, such as
     *  mx.core::UIComponent.
     */
    private static function getTypeHierarchy(object:IStyleClient, qualified:Boolean=true):OrderedObject
    {
        var styleManager:IStyleManager2 = getStyleManager(object);
        var className:String = getQualifiedClassName(object);
        var hierarchy:OrderedObject = styleManager.typeHierarchyCache[className] as OrderedObject;
        if (hierarchy == null)
        {
            hierarchy = new OrderedObject();

            var myApplicationDomain:ApplicationDomain;
            var factory:IFlexModuleFactory = ModuleManager.getAssociatedFactory(object);
            if (factory != null)
            {
                myApplicationDomain = ApplicationDomain(factory.info()["currentDomain"]);
            }
            else
            {
                var myRoot:DisplayObject = SystemManager.getSWFRoot(object);
                if (!myRoot)
                    return hierarchy;
                myApplicationDomain = myRoot.loaderInfo.applicationDomain;
            }

            styleManager.typeHierarchyCache[className] = hierarchy;
            while (!isStopClass(className))
            {
                try
                {
                    var type:String;
                    if (qualified)
                        type = className.replace("::", ".");
                    else
                        type = NameUtil.getUnqualifiedClassName(className);

                    hierarchy.object_proxy::setObjectProperty(type, true);
                    className = getQualifiedSuperclassName(
                        myApplicationDomain.getDefinition(className));
                }
                catch(e:ReferenceError)
                {
                    className = null;
                }
            }
        }
        return hierarchy;
    }

    /**
     *  @private
     *  Our style type hierarhcy stops at UIComponent, UITextField or
     *  GraphicElement, not Object.
     */  
    private static function isStopClass(value:String):Boolean
    {
        return value == null ||
               value == "mx.core::UIComponent" ||
               value == "mx.core::UITextField" ||
               value == "mx.graphics.baseClasses::GraphicElement";
    }

    /**
     *  @private  
     *  Find all matching style declarations for an IAdvancedStyleClient
     *  component. The result is unsorted in terms of specificity, but the
     *  declaration order is preserved.
     *
     *  @param declarations - a map of declarations to be searched for matches.
     *  @param object - an instance of the component to match.
     *
     *  @return An unsorted Array of matching style declarations for the given
     *  subject.
     */
    private static function matchStyleDeclarations(declarations:Array,
            object:IAdvancedStyleClient):Array // of CSSStyleDeclaration
    {
        var matchingDecls:Array = [];

        // Find the subset of declarations that match this component
        for each (var decl:CSSStyleDeclaration in declarations)
        {
            if (decl.matchesStyleClient(object))
                matchingDecls.push(decl);
        }

        return matchingDecls;
    }

    /**
     *  @private
     *  Sort algorithm to order style declarations by specificity. Note that 
     *  Array.sort() is not used as it does not employ a stable algorithm and
     *  CSS requires the order of equal style declaration to be preserved.
     */ 
    private static function sortOnSpecificity(decls:Array):Array // of CSSStyleDeclaration 
    {
        // TODO (pfarland): Copied algorithm from Group.sortOnLayer as the
        // number of declarations to be sorted is usually small. We may consider
        // replacing this insertion sort with an efficient but stable merge sort
        // or the like if many style declarations need to sorted.
        var len:Number = decls.length;
        var tmp:CSSStyleDeclaration;

        if (len <= 1)
            return decls;

        for (var i:int = 1; i < len; i++)
        {
            for (var j:int = i; j > 0; j--)
            {
                if (decls[j].specificity < decls[j-1].specificity)
                {
                    tmp = decls[j];
                    decls[j] = decls[j-1];
                    decls[j-1] = tmp;
                }
                else
                {
                    break;
                }
            }
        }

        return decls; 
    }
    
    /**
     *  @private
     *  Get the style manager of any object. If the object does not implement IFlexModule or
     *  is not of type StyleProxy, then the top-level style manager will be returned.
     * 
     *  @param object - Typed as Object because various interfaces are passed here.
     *  @return a style manager, will not be null.
     */ 
    private static function getStyleManager(object:Object):IStyleManager2
    {
        if (object is IFlexModule)
            return StyleManager.getStyleManager(IFlexModule(object).moduleFactory);
        else if (object is StyleProxy)
            return getStyleManagerFromStyleProxy(StyleProxy(object));
        else
            return StyleManager.getStyleManager(null);
    }

    /**
     *  @private
     *  Get the style manager for a given StyleProxy object.
     * 
     *  @return a style manager, will not be null.
     */ 
    private static function getStyleManagerFromStyleProxy(obj:StyleProxy):IStyleManager2
    {
        // StyleProxy's usually have sources that are DisplayObject's, but a StyleProxy can also have 
        // another StyleProxy as it's source (Example: CalendarLayout's source is a StyleProxy for DateChooser, 
        // whose style is a StyleProxy for DateField)
        var curObj:IStyleClient = obj;
        while (curObj is StyleProxy)
        {
            curObj = StyleProxy(curObj).source;
        }

        if (curObj is IFlexModule)
            return StyleManager.getStyleManager(IFlexModule(curObj).moduleFactory);
        
        return StyleManager.getStyleManager(null);
    }
}

}
