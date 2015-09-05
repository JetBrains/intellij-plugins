package com.test
{
    // line comment 1
import mx.messaging.AbstractConsumer;
import mx.rpc.events.AbstractEvent;

/**
     * doc comment 2
     */
public class ImportsPositioningWithComments
{
    // line comment 3
    /* block comment 4 */
    public function f1():void
    {
        /* block comment 5*/
        // line comment 6
        /**
         * doc comment 7
         */

        import mx.messaging.AbstractProducer;
        import mx.rpc.AbstractOperation;

        function f2():void
        {
            import mx.messaging.AbstractMessageStore;
            import mx.messaging.messages.AbstractMessage;
            import mx.rpc.AbstractInvoker;
            import mx.rpc.AbstractService;

            // line comment 8
            /* block comment 9 */
            var f:AbstractOperation;
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

import mx.charts.BarChart;
import mx.effects.easing.Back;

// line comment 10
/* block comment 11 */

var a:Back;
// line comment 12
function d():void
{
    import mx.charts.series.BarSeries;

    /**
     * doc comment 13
     */
    var b:BarChart;
    var c:BarSeries;
}
//dummy