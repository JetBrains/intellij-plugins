import "dart:html";

main() {
  var form;
  form..add("baz")..add("bar");
  form.children
    ..add(new ParagraphElement()
    ..children.add(new LabelElement()
    ..classes.add("FormLabel")
    ..attributes["for"] = "PlayerName"
    ..text = "Player name:")
    ..children.add(_playerNameInput = new InputElement(type: "text")
    ..id = "PlayerName"
    ..classes.add("FormInput")
    ..spellcheck = false
    ..onKeyDown.listen((KeyboardEvent event) {
    if (event.which == 13) _playerAgeInput.focus();
    if (event.which == 27) _playerNameInput.value = "";
  })
    ..onKeyUp.listen((event) => _validate())));
}

_validate() => null;

dynamic _playerNameInput, _playerAgeInput;
