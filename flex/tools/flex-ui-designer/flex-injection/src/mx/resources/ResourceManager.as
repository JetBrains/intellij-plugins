package mx.resources {
import com.intellij.flex.uiDesigner.ResourceBundleProvider;

import flash.errors.IllegalOperationError;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;
import flash.system.ApplicationDomain;
import flash.system.SecurityDomain;
import flash.utils.Dictionary;

import mx.utils.StringUtil;

public class ResourceManager extends EventDispatcher implements IResourceManager {
  private static const EMPTY_RESOURCE_BUNLDE_CONTENT:Dictionary = new Dictionary();

  private var resourceBundleProvider:ResourceBundleProvider;
  private const localeMap:Object = new Dictionary();
  private var project:Object;

  function ResourceManager(project:Object, resourceBundleProvider:ResourceBundleProvider) {
    instance = this;
    this.project = project;
    this.resourceBundleProvider = resourceBundleProvider;
  }

  private static var instance:ResourceManager;
  //noinspection JSUnusedGlobalSymbols
  public static function getInstance():IResourceManager {
    return instance;
  }

  private var _localeChain:Array = ["en_US"];
  public function get localeChain():Array {
    return _localeChain;
  }
  public function set localeChain(value:Array):void {
    _localeChain = value;
  }

  public function loadResourceModule(url:String, update:Boolean = true, applicationDomain:ApplicationDomain = null,
                                     securityDomain:SecurityDomain = null):IEventDispatcher {
    return null;
  }

  public function unloadResourceModule(url:String, update:Boolean = true):void {
  }

  flex::v4_5
  public function addResourceBundle(resourceBundle:IResourceBundle, useWeakReference:Boolean = false):void {
  }

  flex::v4_1
  public function addResourceBundle(resourceBundle:IResourceBundle):void {
  }

  public function removeResourceBundle(locale:String, bundleName:String):void {
  }

  public function removeResourceBundlesForLocale(locale:String):void {
  }

  public function update():void {
    dispatchEvent(new Event(Event.CHANGE));
  }

  public function getLocales():Array {
    return _localeChain;
  }

  public function getPreferredLocaleChain():Array {
    return _localeChain;
  }

  public function getBundleNamesForLocale(locale:String):Array {
    throw new IllegalOperationError("unsupported");
  }

  public function getResourceBundle(locale:String, bundleName:String):IResourceBundle {
    var bundleMap:Dictionary = localeMap[locale];
    var bundle:ResourceBundle;
    if (bundleMap == null) {
      bundleMap = new Dictionary();
      localeMap[locale] = bundleMap;
    }
    else {
      bundle = bundleMap[bundleName];
    }

    if (bundle == null) {
      var resourceBundleContent:Dictionary = resourceBundleProvider.getResourceBundle(project, locale, bundleName);
      if (resourceBundleContent == null) {
        trace("Cannot find resource bundle " + bundleName + " for locale " + locale);
      }
      bundle = new ResourceBundle(locale, bundleName, resourceBundleContent || EMPTY_RESOURCE_BUNLDE_CONTENT);
      bundleMap[bundleName] = bundle;
    }

    return bundle;
  }

  public function findResourceBundleWithResource(bundleName:String, resourceName:String):IResourceBundle {
    var n:int = _localeChain.length;
    for (var i:int = 0; i < n; i++) {
      var locale:String = localeChain[i];
      var bundle:IResourceBundle = getResourceBundle(locale, bundleName);
      if (bundle != null && resourceName in bundle.content) {
        return bundle;
      }
    }

    return null;
  }

  private function findBundle(bundleName:String, resourceName:String, locale:String):IResourceBundle {
    return locale == null ? findResourceBundleWithResource(bundleName, resourceName) : getResourceBundle(locale, bundleName);
  }

  public function getObject(bundleName:String, resourceName:String, locale:String = null):* {
    var resourceBundle:IResourceBundle = findBundle(bundleName, resourceName, locale);
    return resourceBundle == null ? undefined : resourceBundle.content[resourceName];
  }

  public function getString(bundleName:String, resourceName:String, parameters:Array = null, locale:String = null):String {
    var resourceBundle:IResourceBundle = findBundle(bundleName, resourceName, locale);
    if (resourceBundle == null) {
      return null;
    }

    var value:String = String(resourceBundle.content[resourceName]);
    return parameters == null ? value : StringUtil.substitute(value, parameters);
  }

  public function getStringArray(bundleName:String, resourceName:String, locale:String = null):Array {
    var resourceBundle:IResourceBundle = findBundle(bundleName, resourceName, locale);
    if (resourceBundle == null) {
      return null;
    }

    var array:Array = String(resourceBundle.content[resourceName]).split(",");
    var n:int = array.length;
    for (var i:int = 0; i < n; i++) {
      array[i] = StringUtil.trim(array[i]);
    }
    return array;
  }

  public function getNumber(bundleName:String, resourceName:String, locale:String = null):Number {
    var resourceBundle:IResourceBundle = findBundle(bundleName, resourceName, locale);
    return resourceBundle == null ? NaN : Number(resourceBundle.content[resourceName]);
  }

  public function getInt(bundleName:String, resourceName:String, locale:String = null):int {
    var resourceBundle:IResourceBundle = findBundle(bundleName, resourceName, locale);
    return resourceBundle == null ? 0 : int(resourceBundle.content[resourceName]);
  }

  public function getUint(bundleName:String, resourceName:String, locale:String = null):uint {
    var resourceBundle:IResourceBundle = findBundle(bundleName, resourceName, locale);
    return resourceBundle == null ? 0 : uint(resourceBundle.content[resourceName]);
  }

  public function getBoolean(bundleName:String, resourceName:String, locale:String = null):Boolean {
    var resourceBundle:IResourceBundle = findBundle(bundleName, resourceName, locale);
    return resourceBundle == null ? false : String(resourceBundle.content[resourceName]).toLowerCase() == "true";
  }

  public function getClass(bundleName:String, resourceName:String, locale:String = null):Class {
    var resourceBundle:IResourceBundle = findBundle(bundleName, resourceName, locale);
    return resourceBundle == null ? null : (resourceBundle.content[resourceName] as Class);
  }

  flex::v4_5
  public function installCompiledResourceBundles(applicationDomain:ApplicationDomain, locales:Array, bundleNames:Array,
                                                 useWeakReference:Boolean = false):Array {
    return null;
  }

  flex::v4_1
  public function installCompiledResourceBundles(applicationDomain:ApplicationDomain, locales:Array, bundleNames:Array):void {
  }

  public function initializeLocaleChain(compiledLocales:Array):void {
  }
}
}

import flash.utils.Dictionary;

import mx.resources.IResourceBundle;

final class ResourceBundle implements IResourceBundle {
  private var _bundleName:String;
  private var _content:Dictionary;
  private var _locale:String;

  public function ResourceBundle(locale:String, bundleName:String, content:Dictionary) {
    _locale = locale;
    _bundleName = bundleName;
    _content = content;
  }

  public function get bundleName():String {
    return _bundleName;
  }

  public function get content():Object {
    return _content;
  }

  public function get locale():String {
    return _locale;
  }
}