package {

public class NoParseOnMethodOverride extends Base {

    [Event(name="aa", type="flash.events.Event", deprecatedSince="11")]
    [Foo("ssss")]
    [Bar]
    override protected function foo1():void {
        super.foo1();
    }
}

}
