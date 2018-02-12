package {
public class PickVectorTypeInFlexIter {
    import mx.rpc.soap.AbstractWebService;

    public function foo() {
        var o:Object;

        for each (var abstractWebService:AbstractWebService in bar) {
            <caret>
        }
    }

    private function get bar():Vector.<AbstractWebService> {
        return null;
    }
}
}