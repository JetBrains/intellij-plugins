package {
public class ExtendMultipleClasses{}
}

class AnotherClass {}
class SubClass <error descr="Class cannot extend multiple classes">extends ExtendMultipleClasses, AnotherClass</error> {}