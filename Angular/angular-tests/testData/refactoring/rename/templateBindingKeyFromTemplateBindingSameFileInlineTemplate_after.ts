import {Component, Directive, Input} from '@angular/core';

@Directive({
    selector: '[appClicks]',
    standalone: true,
})
export class AppClicksDirective {
    @Input()
    appClicksFoo!: string

    @Input()
    appClicks!: boolean
}

@Component({
    selector: 'test',
    template: `
      <div *appClicks="true; foo<caret>: 'foo'"></div>
      <ng-template [appClicks]="true" [appClicksFoo]="'foo'"></ng-template>/
    `,
    imports: [
        AppClicksDirective
    ]
})
export class ChipComponent {

}
