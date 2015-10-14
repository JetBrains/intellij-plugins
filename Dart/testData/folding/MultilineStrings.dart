var z = <fold text='"""..."""' expand='true'>"""
something
"""</fold>;

var w = <fold text='r"""..."""' expand='true'>r"""
something"""</fold>;

class Patho <fold text='{...}' expand='true'>{
  String stringify() <fold text='{...}' expand='true'>{
    String stringer() <fold text='{...}' expand='true'>{
      var str = <fold text='"""..."""' expand='true'>"""
It may be weird to have a string here.
"""</fold>;
      return str;
    }</fold>
    return stringer();
  }</fold>
}</fold>
