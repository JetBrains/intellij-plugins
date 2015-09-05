package {
interface IBar {}
class Base implements IBar, IFoo {

}

public class Sub extends Base implements IBar {

}

interface IFoo {

}
}
