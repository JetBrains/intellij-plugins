import {Component} from '@angular/core';
import {AppClicksDirective} from "./templateBindingKeyFromFieldDifferentFile"

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
