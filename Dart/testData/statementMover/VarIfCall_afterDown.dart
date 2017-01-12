class C2a extends C2 {
  C2a();
  int m() => 4;
  static int n() => 7;
  garvl(x) {
    m();
    if (x) n(); else {
      int i=0;
      m();
    }
    do i++;
    while (i < 3);
  }
}
