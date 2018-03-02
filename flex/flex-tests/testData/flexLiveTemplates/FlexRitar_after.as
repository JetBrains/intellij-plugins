package {
public class FlexRitar {
    public function FlexRitar() {
        for (var i:int = arguments.length - 1; i >= 0; i--) {
            var argument:ISomething = arguments[i];
            <caret>
        }
    }
}
}