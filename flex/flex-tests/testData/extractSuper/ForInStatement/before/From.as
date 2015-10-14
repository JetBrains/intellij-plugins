package {

public class Main {
  public function Main() {
    var columns:Vector.<Box> = new <Box>[new Box()];
    for each (var column:Box in columns) {
        column.sleep();
    }
    var rows:Vector.<Box> = new <Box>[new Box()];
    for each (var row:Box in rows) {
        row.wakeup();
    }

    var v;
    if (v is Box) {}
    trace(v as Box);
  }
}
}

class Box {
  internal function sleep():void {}
  internal function wakeup():void {}
}