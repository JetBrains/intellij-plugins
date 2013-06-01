import 'Reference7Helper.dart' as helper;

class Reference{
  helper.Bar getBar(){
    return new helper.Bar();
  }
  main(){
    getBar().getBaz().te<caret>st();
  }
}