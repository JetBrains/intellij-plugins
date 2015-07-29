package com.test
{

public class ImportsPositioning
{
    import mx.rpc.AbstractService;

    public function f1():void
    {
        import mx.messaging.messages.AbstractMessage;

        function f2():void
        {

            var f:AbstractOperation;
            import mx.messaging.AbstractProducer;
            function f3():void
            {
                import mx.rpc.events.AbstractEvent;

                var a:AbstractConsumer;
                var b:AbstractEvent;
                var c:AbstractInvoker;
                import mx.messaging.AbstractConsumer;
                var d:AbstractMessage;
                var e:AbstractMessageStore;
                var g:AbstractProducer;
                var h:AbstractService;
            }
            import mx.messaging.AbstractMessageStore;
        }
    }
    import mx.rpc.AbstractInvoker;
}
import mx.rpc.AbstractOperation;
}

var a:Back;
import mx.effects.easing.Back;

function d():void
{
    import mx.charts.series.BarSeries;
    var b:BarChart;
    var c:BarSeries;
}
import mx.charts.BarChart;
//dummy