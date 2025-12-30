import {Component, Directive, Input} from '@angular/core';

@Directive({
    selector: '[appClicks]',
    inputs: ['app: app<caret>ClicksFoo'],
    standalone: true,
})
export class AppClicksDirective {
    app!: string

    @Input()
    appClicks!: boolean
}

@Component({
    selector: 'test',
    template: `
      <div *appClicks="true; foo: 'foo'"></div>
      <ng-template [appClicks]="true" [appClicksFoo]="'foo'"></ng-template>/
    `,
    imports: [
        AppClicksDirective
    ]
})
export class ChipComponent {

}
