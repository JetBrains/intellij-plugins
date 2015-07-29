package foo {
public class To {

    private var bb = From.func();
    
    public function To() {
        if (From.func() > 5) {
            From.func();
        }
    }
}
}