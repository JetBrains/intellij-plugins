import {Directive} from '@angular/core';
import {BoldDirective} from "./bold.directive";
import {UnderlineDirective} from "./underline.directive";

@Directive({
    selector: '[appMouseenter]',
    standalone: true,
    exportAs: "mouse,boldDir",
    hostDirectives: [{
        directive: BoldDirective,
        outputs: ['hover']
    }, {
        directive: UnderlineDirective,
        inputs: ['color: underlineColor', 'shade : ']
    }, Foo]
})
export class MouseenterDirective {
    constructor() {
    }
}
