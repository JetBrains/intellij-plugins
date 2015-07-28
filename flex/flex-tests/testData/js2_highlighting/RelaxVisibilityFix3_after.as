package {

}

class U {
  internal var foo:String;
}

class B extends U {
}

class C extends U {
  private var parent:B;

  public function zz():void {
    parent.foo = "a";
  }
}
