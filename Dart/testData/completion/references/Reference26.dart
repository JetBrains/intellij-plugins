library Location;

class Location {
  num lat, lng;
  num rotatePos = 0;

  Location(num lat, num lng, {bool bar:false, bool baz}) {
    this.lat = lat; this.lng = lng;
  }

  void flipFlags({bool on, bool up, bool hidden:false}) {

  }
}

void main() {
  var loc = new Location(1, 2, b<caret>);
}