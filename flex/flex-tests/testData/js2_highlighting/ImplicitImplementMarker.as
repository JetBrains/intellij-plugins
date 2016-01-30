interface <lineMarker descr="Has implementations"><info>IFoo</info></lineMarker> {
    function <lineMarker descr="Is implemented"><info descr="instance method">foo</info></lineMarker>();
}

class <lineMarker descr="Has subclasses"><info>Base</info></lineMarker> {
    public function <info descr="instance method">foo</info>() {}
}

class <info>Subclass</info> extends <info>Base</info> implements <info>IFoo</info> {

}