class Gts2 extends Resizable {
}
class Resizable implements IResizable {
  <caret>resize() {}
}
interface IResizable {
  resize();
}