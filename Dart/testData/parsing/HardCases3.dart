typedef ObservableBase = Object with ObservableMixin;

@DocsEditable
@DomName('AbstractWorker')
class AbstractWorker extends EventTarget {
  AbstractWorker.internal() : super.internal();

  static const EventStreamProvider<Event> errorEvent = const EventStreamProvider<Event>('error');

  @DocsEditable
  @DomName('EventTarget.addEventListener, EventTarget.removeEventListener, EventTarget.dispatchEvent')
  AbstractWorkerEvents get on =>
    new AbstractWorkerEvents(this);

  @DocsEditable
  @DomName('AbstractWorker.addEventListener')
  void $dom_addEventListener(String type, EventListener listener, [bool useCapture]) native "AbstractWorker_addEventListener_Callback";

  @DocsEditable
  @DomName('AbstractWorker.dispatchEvent')
  bool $dom_dispatchEvent(Event evt) native "AbstractWorker_dispatchEvent_Callback";

  @DocsEditable
  @DomName('AbstractWorker.removeEventListener')
  void $dom_removeEventListener(String type, EventListener listener, [bool useCapture]) native "AbstractWorker_removeEventListener_Callback";

  Stream<Event> get onError => errorEvent.forTarget(this);
}

main () {
  const TEST = "foo";
  var map = {
    TEST : 'some value'
  };
}