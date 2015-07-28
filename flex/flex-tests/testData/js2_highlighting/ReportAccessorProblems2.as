package {
    public class ReportAccessorProblems2 {
        <error descr="Flash compiler bug 174646: Get accessor method access type is different from set accessor access type, expecting 'internal'">public</error> function get foo():* {
            return null;
        }

        function set <error descr="Flash compiler bug 174646: Set accessor method access type is different from get accessor access type, expecting 'public'">foo</error>(value:*):void {
        }
    }
}
