import com.*;

class com.Header
{
  function Header(num:Number) {
    var ctrl:Number<caret> = Referencer.getController(num);
    if (true) Header(ctrl)
    var z:Referencer2;
    Header2(1);
    this.Header2(1);
  }
  native function Header2(num:Number);
}

class com.Referencer2 {}