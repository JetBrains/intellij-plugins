import {Component, signal} from '@angular/core';

@Component({
  selector: 'app-root',
  template: '',
})
export class AppComponent {

  test = signal<string>("")

  foo() {
    this.test.<warning descr="Accessing length property of an uncalled signal. Did you mean to call the signal first?">len<caret>gth</warning>
  }
}
