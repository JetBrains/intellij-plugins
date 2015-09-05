package {
import flash.events.Event;

class Ref {

  public function Ref() {
    new SuperClass(1, 2, "foo", new Event("foo"));
  }
}
}