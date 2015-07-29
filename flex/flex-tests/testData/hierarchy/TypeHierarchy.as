package mypack {
public class SomeClass{}
}

package abc.def {
 class SubClass1 extends mypack.SomeClass{}
 class SubClass2 extends mypack.SomeClass{}
}

package {
 class SubSubClass1 extends abc.def.SubClass1{}
 [ExcludeClass]
 class ExludedClass extends abc.def.SubClass1{}
}