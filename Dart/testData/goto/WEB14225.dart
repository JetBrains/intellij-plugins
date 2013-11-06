class Location {
  num lat, lng; // instance variables

  Location(num lat, num lng) { // constructor with instance parameters
    this.lat = lat;
    this.lng = lng;
  }
}

void main() {
  var waikiki = new Location(21.271488, -157.822806); // Object instance instantiation

  query("#h1")..text = wai<caret>kiki.lat.toStringAsFixed(2); // waikiki.lat.toStringAsFixed(2) not resolved
}