class ContentComponent {

  void ngDoCheck() {
    if (_differ != null) {
      var changes = this._differ.diff(data);
      _cdr.detectChanges();
      if (changes != null) {
        _cdr.markForCheck();
        selectedItem = selectedItem;
      }
    }
  }

  var selectedItem;
  var _differ;
  var data;
  var _cdr;
}
