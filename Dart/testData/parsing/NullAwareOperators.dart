class NAO1 {
  f(x,y,z) {
    var a = (x)?.y;
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
  }
}

fnao1(x) => x?.op();
fnao2(x) => x ?? "other";
fnao3(x) {
  var y;
  y ??= x;
  return y;
}
