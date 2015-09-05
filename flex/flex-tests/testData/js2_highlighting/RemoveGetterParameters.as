package {

public class RemoveGetterParameters {
    public function get foo<error descr="A getter definition must have no parameters">(i: String, b:Boolean)</error>:int { return 0; }
}

}