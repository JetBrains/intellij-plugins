import {Component} from '@angular/core';
import {AppClicksDirective} from "./templateBindingKeyFromFieldDifferentFile"

@Component({
    selector: 'test',
    template: `
      <div *appClicks="true; pa<caret>ram: 'foo'"></div>
      <ng-template [appClicks]="true" [appClicksParam]="'foo'"></ng-template>/
    `,
    imports: [
        AppClicksDirective
    ]
})
export class ChipComponent {

}
