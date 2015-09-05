package {

[Suite]
[RunWith("org.flexunit.runners.Suite")]
public class <warning descr="Test suite is empty">EmptySuite</warning> {
    var foo : MyTest;

    private var foo2 : MyTest;

    public static var foo3 : MyTest;

    public static var foo4 : <error>SomeType</error>;

    //public var foo4 : String;

    public var foo5 : int;

    public var foo6;

    public var foo7 : *;
    
    //public var foo8 : Number;

}
}