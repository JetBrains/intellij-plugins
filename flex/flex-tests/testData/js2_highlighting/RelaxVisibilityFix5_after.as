package {

}

class U {
  protected var foo:String;
}

class B extends U {
  public function zz():void {
    var v : U;
    v.foo = "a";
  }
}
