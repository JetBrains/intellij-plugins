interface <lineMarker descr="Has implementations">IFoo</lineMarker> {
    function <lineMarker descr="Is implemented">foo</lineMarker>();
}

class <lineMarker descr="Has subclasses">Base</lineMarker> {
    public function foo() {}
}

class Subclass extends Base implements IFoo {

}