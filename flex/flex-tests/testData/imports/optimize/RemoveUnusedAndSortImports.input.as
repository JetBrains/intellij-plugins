package com.test
{
// comment before
import com.test.pack1.ClassName;
import com.test.pack2.ClassName;
import com.test.pack3.ClassName;
import mx.rpc.events.FaultEvent;
import com.test.RemoveUnusedAndSortImports;
import com.test.SamePackClass;
import flash.display.CapsStyle;
import mx.rpc.events.FaultEvent;
import flash.filters.BitmapFilter;
import mx.rpc.soap.AbstractWebService;
import mx.rpc.events.AbstractEvent;
import mx.binding.Binding;
import mx.rpc.events.HeaderEvent;
import mx.rpc.AbstractOperation;
// comment after

public class RemoveUnusedAndSortImports
{
    var a:Binding;
    var c:String;
    var d:int;
    var e:AbstractWebService;
    var f:AbstractOperation;
    var g:FaultEvent;
    var h:HeaderEvent;
    var i:BitmapFilter;
    var j:SamePackClass;
    var k:RemoveUnusedAndSortImports;
    var l:com.test.pack1.ClassName;

    public function func():void
    {
        import com.test.pack2.ClassName;
        import com.test.pack1.ClassName;

        var n:com.test.pack2.ClassName;
    }
}
}

import flash.filters.BitmapFilter;
import mx.utils.Base64Encoder;
import mx.utils.Base64Decoder;

var x:Base64Decoder;
var y:Base64Encoder;
var z:Boolean;
