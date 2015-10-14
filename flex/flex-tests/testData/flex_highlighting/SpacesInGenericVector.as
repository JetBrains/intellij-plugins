package {
import flash.display.Sprite;

public class SpacesInGenericVector {
  public function foo():void {
    var v:Vector .< Vector.< int >  >;
    v = new Vector.<  Vector.<int  > >();
    v = new Vector.<  Vector.<int  > >();
    v = <error>new Vector.<Vector.<uint>>()</error>;
    var v1:Vector.< Vector.< Sprite >  > = new Vector.<Vector.<  Sprite>>();
    v1 = new Vector.<Vector.<Sprite>>();
    var v2:Vector.<String > = new Vector.<String>();
    var v3:Vector.<String> = new Vector.<String >();
    var v4:Vector.<Sprite > = new Vector.<Sprite>();
    var v5:Vector.<Sprite> = new Vector.<Sprite >();
    var v6:Vector
                 .< Sprite > = new Vector  .<  Sprite  >();
  }
}
}