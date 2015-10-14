package {
interface IBar {}
class Base implements IBar {

}

public class Sub extends Base implements IFoo, IBar {

}

interface IFoo {

}
}
