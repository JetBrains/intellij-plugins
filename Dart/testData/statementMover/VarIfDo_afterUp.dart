class C2a extends C2 {
  C2a();
  int m() => 4;
  static int n() => 7;
  garvl(x) {
    m();
    n();
    if (x) n(); else {
      m();
      int i=0;
    }
    do i++;
    while (i < 3);
  }
}
