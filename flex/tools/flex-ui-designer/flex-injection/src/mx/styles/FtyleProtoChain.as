package mx.styles {
import mx.core.IInvalidating;
import mx.core.IUITextField;

public class FtyleProtoChain {
  public static const STYLE_UNINITIALIZED:Object = {};

  public static function getClassStyleDeclarations(object:IStyleClient):Array {
    return null;
  }

  public static function initProtoChain(object:IStyleClient, inheritPopUpStylesFromOwner:Boolean = true):void {

  }

  public static function initProtoChainForUIComponentStyleName(obj:IStyleClient):void {

  }

  public static function initTextField(obj:IUITextField):void {
  }

  public static function styleChanged(object:IInvalidating, styleProp:String):void {
  }

  public static function matchesCSSType(object:IAdvancedStyleClient, cssType:String):Boolean {
    return false;
  }

  public static function getMatchingStyleDeclarations(object:IAdvancedStyleClient, styleDeclarations:Array):Array {
    return null;
  }
}
}
