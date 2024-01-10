import {Directive} from '@angular/core';

@Directive({
    selector: '[appUnderline]',
    standalone: true,
    exportAs: "underline",
})
export class UnderlineDirective {
}


