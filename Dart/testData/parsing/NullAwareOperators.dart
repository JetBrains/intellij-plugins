var a = b ? [5] : [6];
var a = b ? [3, 2, 1, 0].contains("") : [6];
var a = b ? [3, 2, 1, 0].contains("")[1].foo().bar()[2][3]<T>! : [6];
var a = b ? [] : {};
var a = b ? {} : {};
var a = b ? {} : [];

var x = y ? [5];
var x = y ? [];

class NAO1 {
  f(x,y,z) {
    var a = (x)?.y?.[b];
    var g = y?[b];
    var h = y?[b] ?? c?..[0];
    var b = x?.y;
    var c = x?.z();
    var e = a ?? b ? y : z;
    var f = x ?? y ?? z, g, h;
    e = a ?? b ? y : z;
    f = x ?? y ?? z;
    g = x || y ?? z;
    h = x ?? y || z;
    a ??= c;
    a ||= c;
    a &&= c;
    this?.f(e, f, g ?? h);

    aaa!.bcc!()! as CCC ..ddd()!![][]![]!();
  }
}

fnao1(x) => x?.op();
fnao2(x) => x ?? "other";
fnao3(x) {
  b? c;
  b? c = d;
  b? c:d;
  e = f ? g : h;

  var y;
  y ??= x;
  return y;
}

static final Map<String?, Map<int?, bool?>?>? a;

const x = true ? 2 : 3 is int;
var y1 = x is int ? 0 : 1;
var y2 = x is int ? ? 0 : 1;
var y3 = x is! int ? 0 : 1;
var y4 = x is! int ? ? 0 : 1;
var y5 = x as int ? 0 : 1;
var y6 = x as int ? ? 0 : 1;
var y7 = x is Function() ? 0 : 1;
var y8 = x is Function() ? ? 0 : 1;
var y9 = x is List<Function> Function<A>(Function x) Function() ? 0 : 1;
var y0 = x is List<Function> Function<A>(Function x) Function() ? ? 0 : 1;

var set1 = { x as bool ? -3 : 3};
var set2 = { x is bool ? -3 : 3};
var set3 = { x as bool ? ? -3 : 3};
var set4 = { x is bool ? ? -3 : 3};

var map1 = { x as bool ? -3 : 3 : 5};
var map2 = { x is bool ? -3 : 3 : 5};
var map3 = { x as bool ? ? -3 : 3 : 5};
var map4 = { x is bool ? ? -3 : 3 : 5};
