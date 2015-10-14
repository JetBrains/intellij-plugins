package com.test
{
// comment before
import com.test.pack1.ClassName;

import flash.filters.BitmapFilter;

import mx.binding.Binding;
import mx.rpc.AbstractOperation;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.HeaderEvent;
import mx.rpc.soap.AbstractWebService;

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
    var l:ClassName;

    public function func():void
    {
        import com.test.pack2.ClassName;

        var n:com.test.pack2.ClassName;
    }
}
}

import mx.utils.Base64Decoder;
import mx.utils.Base64Encoder;

var x:Base64Decoder;
var y:Base64Encoder;
var z:Boolean;
