import {Component, Directive, Input} from '@angular/core';

@Directive({
    selector: '[test]',
    standalone: true,
})
export class TestDirective {
    @Input('foo-bar') fooBar!: number;<caret>

}

@Component({
    selector: 'app-root',
    imports: [TestDirective],
    standalone: true,
    template: `
      <div test [foo-bar]="123"></div>`
})
export class AppComponent {
}
