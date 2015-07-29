package {
public final class ExtendFinalClass{}
}

class SubClass extends <error descr="Cannot extend final class 'ExtendFinalClass'">ExtendFinalClass</error> {}