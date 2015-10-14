package {

}

class U {
  protected var foo:String;
}

class B extends U {
}

class C extends U {
  private var parent:B;

  public function zz():void {
    parent.<error>foo</error> = "a";
  }
}
