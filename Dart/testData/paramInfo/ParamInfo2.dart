class ParamInfo2 extends Inner<Node> {
  foo() {
    bar(10, "", <caret>);
  }
}

class Inner<T> {
  bar(int p1, p2, T p3) {

  }
}

class Node {

}