package {
public interface ITest { function test<caret>Me():void; }
}

package {
public class TestImpl implements ITest{

public function testMe():void {
}
}
}

package {
public class test {
function Test():void
{ var t:ITest = new TestImpl(); t.testMe();
  TestImpl(t).testMe();
}

}
}