package com.test
{
    // line comment 1
import mx.rpc.events.AbstractEvent;
    /**
     * doc comment 2
     */
public class ImportsPositioningWithComments
{
    // line comment 3
    import mx.messaging.AbstractConsumer;
    /* block comment 4 */
    public function f1():void
    {
        /* block comment 5*/
        // line comment 6
        /**
         * doc comment 7
         */
        import mx.rpc.AbstractOperation;

        function f2():void
        {
            import mx.messaging.AbstractMessageStore;
            // line comment 8
            import mx.messaging.messages.AbstractMessage;
            /* block comment 9 */
            import mx.rpc.AbstractInvoker;

            var f:AbstractOperation;
            import mx.rpc.AbstractService;
            var a:AbstractConsumer;
            var b:AbstractEvent;
            var c:AbstractInvoker;
            var d:AbstractMessage;
            var e:AbstractMessageStore;
            var g:AbstractProducer;
            var h:AbstractService;
        }
        import mx.messaging.AbstractProducer;
    }
}
}
// line comment 10
/* block comment 11 */

var a:Back;
// line comment 12
import mx.effects.easing.Back;

function d():void
{
    import mx.charts.series.BarSeries;
    /**
     * doc comment 13
     */
    var b:BarChart;
    var c:BarSeries;
}
import mx.charts.BarChart;
//dummy