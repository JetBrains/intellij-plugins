package {
interface IBar {}
class Base implements IBar, IFoo {

}

public class Sub extends Base implements IZzz, IBar {

}

interface IFoo {

}
}
