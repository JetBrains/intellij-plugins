import {Component, Directive, Input} from '@angular/core';

@Directive({
    selector: '[appClicks]',
    standalone: true,
})
export class AppClicksDirective {
    @Input()
    appClicksParam!: string

    @Input()
    appClicks!: boolean
}

@Component({
    selector: 'test',
    template: `
      <div *appClicks="true; par<caret>am: 'foo'"></div>
      <ng-template [appClicks]="true" [appClicksParam]="'foo'"></ng-template>/
    `,
    imports: [
        AppClicksDirective
    ]
})
export class ChipComponent {

}
