function AppComponent() {}

AppComponent.annotations = [
  new angular.DirectiveAnnotation({
    selector: '[my-customer]'
  })
];

document.addEventListener('DOMContentLoaded', function() {
  angular.bootstrap(AppComponent);
});