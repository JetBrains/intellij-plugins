import {Component, Directive, Input} from '@angular/core';

@Directive({
    selector: '[appClicks]',
    standalone: true,
})
export class AppClicksDirective {
    @Input()
    appClic<caret>ksParam!: string

    @Input()
    appClicks!: boolean
}

@Component({
    selector: 'test',
    templateUrl: './templateBindingKeyFromFieldSameFileExternalTemplate.html',
    imports: [
        AppClicksDirective
    ]
})
export class ChipComponent {

}
