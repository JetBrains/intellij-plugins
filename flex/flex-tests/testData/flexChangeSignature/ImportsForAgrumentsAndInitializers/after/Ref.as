package {
import bar.Yyy;
import bar.Zzz;

import com.Bar;
import com.Const;
import com.Foo;

import uuu.glob;

class Ref {
  public function name():void {
    var v : From;
    v.foo(new Foo(), Bar.SIZE, Foo.MESSAGE, Zzz.func(new Yyy(), glob(Const)), unresolved);
    v.foo(new Foo(), Bar.SIZE, Foo.MESSAGE, Zzz.func(new Yyy(), glob(Const)), unresolved);
  }

  public function zzz():void {
    var v : From;
    v.foo(new Foo(), Bar.SIZE, Foo.MESSAGE, Zzz.func(new Yyy(), glob(Const)), unresolved);
  }
}
}

import bar.Yyy;
import bar.Zzz;

import com.Bar;
import com.Const;
import com.Foo;

import uuu.glob;

class Aux {
  private function xxx():void {
    var v : From;
    v.foo(new Foo(), Bar.SIZE, Foo.MESSAGE, Zzz.func(new Yyy(), glob(Const)), unresolved);
    v.foo(new Foo(), Bar.SIZE, Foo.MESSAGE, Zzz.func(new Yyy(), glob(Const)), unresolved);
  }
}