function AppComponent() {}

AppComponent.annotations = [
  new angular.ComponentAnnotation({
    selector: 'my-customer'
  })
];

document.addEventListener('DOMContentLoaded', function() {
  angular.bootstrap(AppComponent);
});