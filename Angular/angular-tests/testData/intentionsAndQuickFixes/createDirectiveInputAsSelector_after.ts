import {Component, Directive, Input} from '@angular/core';

@Directive({
    selector: '[test]',
    standalone: true,
})
export class TestDirective {
    @Input() test!: number;<caret>
}

@Component({
    selector: 'app-root',
    imports: [TestDirective],
    standalone: true,
    template: `
      <div [test]="123"></div>`
})
export class AppComponent {
}
