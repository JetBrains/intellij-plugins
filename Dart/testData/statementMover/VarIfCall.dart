class C2a extends C2 {
  C2a();
  int m() => 4;
  static int n() => 7;
  garvl(x) {
    m();
<caret>    int i=0;
    if (x) n(); else {
      m();
    }
    do i++;
    while (i < 3);
  }
}
