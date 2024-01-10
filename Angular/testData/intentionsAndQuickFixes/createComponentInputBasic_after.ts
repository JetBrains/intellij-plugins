import {Component, Input} from '@angular/core';

@Component({
    selector: 'app-test',
    standalone: true,
    template: ``
})
export class TestComponent {
    @Input() foo!: number;<caret>


}

@Component({
    selector: 'app-root',
    imports: [TestComponent],
    standalone: true,
    template: `
      <app-test [foo]="123"></app-test>`
})
export class AppComponent {
}
