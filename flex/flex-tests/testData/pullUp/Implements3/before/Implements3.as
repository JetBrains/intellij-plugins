package {
interface IBar {}
class Base implements IBar {

}

public class Sub extends Base implements IZzz, IFoo, IBar {

}

interface IFoo {

}
}
