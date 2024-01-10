import {Directive} from '@angular/core';

@Directive({
    selector: '[appBold]',
    standalone: true,
    exportAs: "  bolder  ,  bar "
})
export class BoldDirective {
}
