package {

public class Main {
  public function Main() {
    var columns:Vector.<Box> = new <Box>[new BoxImpl()];
    for each (var column:Box in columns) {
        column.sleep();
    }
    var rows:Vector.<BoxImpl> = new <BoxImpl>[new BoxImpl()];
    for each (var row:BoxImpl in rows) {
        row.wakeup();
    }

    var v;
    if (v is BoxImpl) {}
    trace(v as BoxImpl);
  }
}
}

class Box {
    internal function sleep():void {
    }
}
class BoxImpl extends Box {
    internal function wakeup():void {
    }
}