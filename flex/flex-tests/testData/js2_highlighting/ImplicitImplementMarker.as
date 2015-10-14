interface <lineMarker descr="Has implementations"></lineMarker><info>IFoo</info> {
    function <lineMarker descr="Is implemented"></lineMarker><info descr="instance method">foo</info>();
}

class <lineMarker descr="Has subclasses"></lineMarker><info>Base</info> {
    public function <info descr="instance method">foo</info>() {}
}

class <info>Subclass</info> extends <info>Base</info> implements <info>IFoo</info> {

}