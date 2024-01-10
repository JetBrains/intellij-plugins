import {Component,Directive} from '@angular/core';

@Directive({
    selector: '[test]',
    standalone: true,
})
export class TestDirective {

}

@Component({
    selector: 'app-root',
    imports: [TestDirective],
    standalone: true,
    template: `
      <div test (fo<caret>o)="check($event)"></div>`
})
export class AppComponent {

  check(value: {bar: number}) {

  }

}
