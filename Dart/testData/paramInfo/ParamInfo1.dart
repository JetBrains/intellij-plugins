class ParamInfo1 extends Inner<Node> {
  foo() {
    bar(<caret>);
  }
}

class Inner<T> {
  bar(int p1, p2, T p3) {

  }
}

class Node {

}