package {
class VectorElementTypeIncompatible {
    function foo():void {
        var x:Vector.<String> = <error>new <int>[1]</error>;
        var x2:Vector.<int> = new <int>[1];
        x = <error>x2</error>;
        var v:Vector.<int> = new Vector.<int>;
        var v2:Vector.<int> = v.reverse();
        var notCompatible:Vector.<Object> = <error>new Vector.<String>()</error>;

        var v11:Vector.<int> = getVectorInt();
        var v12:Vector.<int> = getVectorInt().reverse();
        var v13:Vector.<int> = getVectorInt().concat().reverse();
        var v14:Vector.<int> = getThis().getVectorInt().concat().concat();
        var v15:Vector.<int> = getThis().getThis().getVectorInt().concat().reverse().concat();

        var v21:Vector.<int> = <error descr="Initializer type Vector.<String> is not assignable to variable type Vector.<int>">getVectorString()</error>;
        var v22:Vector.<int> = <error descr="Initializer type Vector.<String> is not assignable to variable type Vector.<int>">getVectorString().reverse()</error>;
        var v23:Vector.<int> = <error descr="Initializer type Vector.<String> is not assignable to variable type Vector.<int>">getVectorString().concat().reverse()</error>;
        var v24:Vector.<int> = <error descr="Initializer type Vector.<String> is not assignable to variable type Vector.<int>">getThis().getVectorString().concat().concat()</error>;
        var v25:Vector.<int> = <error descr="Initializer type Vector.<String> is not assignable to variable type Vector.<int>">getThis().getThis().getVectorString().concat().reverse().concat()</error>;

        var v26:Vector.<Vector.<uint>> = new Vector.<Vector.<uint>>();
        var v27:Vector.<Vector.<uint>> = new <Vector.<uint>>[];
    }

    public function bar():Vector.<String> {
        var v1:Vector.<String> = new Vector.<String>();
        var v2:Vector.<int> = new Vector.<int>();
        v1 = (v1 as Vector.<String>).reverse();
        v1 = v1.reverse();
        v1 = <error>(v2 as Vector.<int>).reverse()</error>;
        return (v1 as Vector.<String>).reverse();
    }

    private function getThis():VectorElementTypeIncompatible {
        return this;
    }

    public function getVectorInt():Vector.<int> {
        return new Vector.<int>();
    }

    public function getVectorString():Vector.<String> {
        return new Vector.<String>();
    }
}
}
