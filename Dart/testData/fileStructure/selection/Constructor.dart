class Constructor {
  int num1;
  int num2;
  <caret>Constructor() {}
  Constructor.one(this.num1);
  Constructor.two(this.num1, this.num2);
}
