package {

}

class U {
  private var foo:String;
}

class B extends U {
  public function zz():void {
    <error>foo</error> = "a";
  }
}
