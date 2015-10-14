package {
    [Bindable]
    public class ReportAccessorProblems4 {
        public function get foo():* {
            return null;
        }

        function set foo(value:*):void {
        }
    }
}
