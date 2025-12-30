import {Component} from '@angular/core';

declare abstract class Foo {
  get bar(): number
  private set bar(value)
}

class Bar extends Foo {}

@Component({
  selector: 'app-root',
  standalone: true,
  template: '{{ formControl.b<caret>ar }}',
})
export class AppComponent {
  formControl = new Bar();
}
