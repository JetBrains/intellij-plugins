package {
import com.a.ClassA;
import com.a.ns1;
<warning>import com.a.someFunction;</warning>
<warning>import com.a.someVariable;</warning>

var <error>p</error> : <warning descr="Qualified name may be replaced with import statement">com.a.ClassA</warning>;
class <error>Foo</error> {
  <warning descr="Qualified name may be replaced with import statement">com.a.ns1</warning> function optimizeImportsFqn(<warning>p</warning>:String) {}
}
<warning descr="Qualified name may be replaced with import statement">com.a.someFunction</warning>();
var <error>v</error> = <warning descr="Qualified name may be replaced with import statement">com.a.someVariable</warning>;<caret>
}
