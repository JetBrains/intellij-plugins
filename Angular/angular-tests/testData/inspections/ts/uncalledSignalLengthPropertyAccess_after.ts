import {Component, signal} from '@angular/core';

@Component({
  selector: 'app-root',
  template: '',
})
export class AppComponent {

  test = signal<string>("")

  foo() {
    this.test().length
  }
}
