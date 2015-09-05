package {
public class AccessInternalMemberFromProperty {
  public function UsingClassWithInternalMember() {
    var obj:Object = {
      FOO: AccessInternalMemberFromProperty_2.FOO,
      foo: new AccessInternalMemberFromProperty_2().foo() // "Element is not accessible" - what?!
    };
  }
}
}