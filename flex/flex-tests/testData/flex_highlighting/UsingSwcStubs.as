import mx.core.IFlexModuleFactory;
import mx.containers.VBox;
import mx.collections.IList;
import mx.collections.ArrayCollection;
import mx.core.mx_internal;
var a:IFlexModuleFactory;

class MyUIComponent extends VBox {
    public var aList : IList = new ArrayCollection();

    public function foo() : void {
        var anotherArray : ArrayCollection = new ArrayCollection();
        aList = anotherArray;
    }

    function aaa():String {
        use namespace mx_internal;
        invalidateSize();
        return layoutObject.direction + direction;
    }

    override protected function createChildren():void {
        super.createChildren();
    }
}