package {

import com.foo.*;

var <error>p</error> : <warning descr="Qualified name may be replaced with import statement">com.foo.Foo</warning>;

var <error>t</error> : <warning descr="Qualified name may be replaced with import statement">com.bar.Bar</warning>;

import com.bar.*;

}
