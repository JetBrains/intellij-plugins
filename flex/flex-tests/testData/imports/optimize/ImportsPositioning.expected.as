package com.test
{
import mx.rpc.AbstractInvoker;
import mx.rpc.AbstractOperation;
import mx.rpc.AbstractService;

public class ImportsPositioning
{
    public function f1():void
    {
        import mx.messaging.messages.AbstractMessage;

        function f2():void
        {
            import mx.messaging.AbstractMessageStore;
            import mx.messaging.AbstractProducer;

            var f:AbstractOperation;
            function f3():void
            {
                import mx.messaging.AbstractConsumer;
                import mx.rpc.events.AbstractEvent;

                var a:AbstractConsumer;
                var b:AbstractEvent;
                var c:AbstractInvoker;
                var d:AbstractMessage;
                var e:AbstractMessageStore;
                var g:AbstractProducer;
                var h:AbstractService;
            }
        }
    }
}
}

import mx.charts.BarChart;
import mx.effects.easing.Back;

var a:Back;
function d():void
{
    import mx.charts.series.BarSeries;

    var b:BarChart;
    var c:BarSeries;
}
//dummy