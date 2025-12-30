import {Component} from '@angular/core';
import {AppClicksDirective} from "./templateBindingKeyFromFieldDifferentFile"

@Component({
    selector: 'test',
    templateUrl: './component.html',
    imports: [
        AppClicksDirective
    ]
})
export class ChipComponent {

}
