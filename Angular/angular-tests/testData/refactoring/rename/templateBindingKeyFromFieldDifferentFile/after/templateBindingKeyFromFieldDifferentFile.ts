import {Directive, Input} from '@angular/core';

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