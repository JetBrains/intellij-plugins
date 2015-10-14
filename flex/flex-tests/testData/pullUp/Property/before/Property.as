package {
public class Super {}

public class Sub extends Super {

    /**
     * this is a prop
     */
    public function get prop() :String {
    }

    public function set prop(p:String) {
    }

    public function func() {
        prop = prop;
    }
}
}
