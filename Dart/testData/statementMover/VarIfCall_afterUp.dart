class C2a extends C2 {
  C2a();
  int m() => 4;
  static int n() => 7;
  garvl(x) {
<caret>    int i=0;
    m();
    if (x) n(); else {
      m();
    }
    do i++;
    while (i < 3);
  }
}
