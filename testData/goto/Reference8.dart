import 'Reference8Helper.dart' as helper;

class Reference{
  main(){
    var baz = new Baz();
    baz.te<caret>st;
  }
}

class Baz extends helper.Bar {
}