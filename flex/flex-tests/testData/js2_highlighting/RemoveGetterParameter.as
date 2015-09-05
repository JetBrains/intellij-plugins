package {

public class RemoveGetterParameter {
    public function get foo<error descr="A getter definition must have no parameters">(i: String)</error>:int { return 0; }
}

}