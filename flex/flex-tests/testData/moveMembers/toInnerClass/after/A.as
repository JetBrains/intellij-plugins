package {

}


class U {

}

class Ggg extends U {

    public static var rootStyleDeclarationProxy:String;
}

class C extends U {


    public function foo():void {
    Ggg.rootStyleDeclarationProxy = "sdf";
  }
}
