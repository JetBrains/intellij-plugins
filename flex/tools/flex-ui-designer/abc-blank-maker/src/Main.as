package {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getDefinitionByName;

public class Main extends Sprite {
  public function Main() {
    //var abcBuilder:IAbcBuilder = new AbcBuilder();
    //var packageBuilder:IPackageBuilder = abcBuilder.definePackage("_");
    //var classBuilder:IClassBuilder = packageBuilder.defineClass("BI", "mx.core.BitmapAsset");

    //BitmapAsset;
    //abcBuilder..addEventListener(Event.COMPLETE, abcBuilder_completeHandler);
    //abcBuilder.buildAndLoad();
    //return;

    //var fileStream:FileStream = new FileStream();
    //fileStream.open(File.desktopDirectory.resolvePath("t.swf"), FileMode.WRITE);
    //fileStream.writeBytes(abcBuilder.buildAndExport());
    //fileStream.close();

    //NativeApplication.nativeApplication.exit();
  }

  private function abcBuilder_completeHandler(event:Event):void {
    var claz:Class = Class(getDefinitionByName("_.BI"));
    var c:Object = new claz;
  }
}
}