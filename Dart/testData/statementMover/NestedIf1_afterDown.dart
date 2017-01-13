class ContentComponent {

  void ngDoCheck() {
    if (_differ != null) {
      var changes = this._differ.diff(data);
      if (changes != null) {
        _cdr.detectChanges();
        selectedItem = selectedItem;
        _cdr.markForCheck();
      }
    }
  }

  var selectedItem;
  var _differ;
  var data;
  var _cdr;
}
