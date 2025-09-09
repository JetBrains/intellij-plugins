import {Component, signal} from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './uncalled-signal-length-property-access.html',
})
export class AppComponent {

  test = signal<string>("")

  bar() {
    return this.test
  }

}
