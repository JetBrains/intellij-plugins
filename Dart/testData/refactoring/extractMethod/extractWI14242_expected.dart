import 'dart:coreimpl';

num i = 0;

void main() {


}

List<String> extracted() {
  var l1 = new List<String>();
  return l1;
}

void smth(){

  i+=2;
  var l1 = extracted();
  var l2 = new List<String>();
  var a = new Arrays();
  a.copy(l1, 1,l2, 1,1) ;
}