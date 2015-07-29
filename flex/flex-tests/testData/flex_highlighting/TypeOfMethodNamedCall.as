class TypeOfMethodNamedCall {
    import flash.external.ExternalInterface;

    public function f(): void {
        var url:String = ExternalInterface.call("window.location.href.toString");
    }

}