import {Component} from '@angular/core';

@Component({
    selector: 'app-test',
    standalone: true,
    template: ``
})
export class TestComponent {


}

@Component({
    selector: 'app-root',
    imports: [TestComponent],
    standalone: true,
    template: `
      <app-test [fo<caret>o]="123"></app-test>`
})
export class AppComponent {
}
