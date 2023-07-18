import {Directive} from '@angular/core';

@Directive({
    selector: '[appBold]',
    standalone: true,
    exportAs: "bold"
})
export class BoldDirective {
}
