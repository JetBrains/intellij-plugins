import com.*;

class com.Header
{
  function Header(num:Number) {
    var <warning>ct<caret>rl</warning> = Referencer.getController(num);
    if (<warning descr="If statement can be simplified">true</warning>) Header(ctrl)
    var z:Referencer2;
    Header2(1);
    this.Header2(1);
  }
  native function <warning>Header2</warning>(num:Number);
}

class com.Referencer2 {}