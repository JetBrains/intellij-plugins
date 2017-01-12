class ContentComponent {

  void ngDoCheck() {
    if (_differ != null) {
      var changes = this._differ.diff(data);
      if (changes != null) {
        _cdr.detectChanges();
<caret>        _cdr.markForCheck();
        selectedItem = selectedItem;
      }
    }
  }

  var selectedItem;
  var _differ;
  var data;
  var _cdr;
}
