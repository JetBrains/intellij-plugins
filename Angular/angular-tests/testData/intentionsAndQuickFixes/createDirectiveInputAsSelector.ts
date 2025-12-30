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
      <div [te<caret>st]="123"></div>`
})
export class AppComponent {
}
