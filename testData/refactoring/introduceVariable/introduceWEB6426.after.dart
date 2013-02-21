import 'dart:html';
import 'dart:math';

class Location1 {
  num lat, lng;


  Location1(num lat, num lng) {
    this.lat = lat;
    this.lng = lng;
  }
}

void main() {
  var lat = 21.271488;
  var lng =  -157.822806;
  var loc = new Location1(lat, lng);

  print(loc.lat);
  var min = min(loc.lat, loc.lng);


  var a = "ltr";
  query("#h1")
    ..text = min.toStringAsFixed(2)
    ..dir = a;
}