class Gts2 extends Resizable {
}
class Resizable implements IResizable {
  <caret>resize() {}
}
class IResizable {
  resize();
}