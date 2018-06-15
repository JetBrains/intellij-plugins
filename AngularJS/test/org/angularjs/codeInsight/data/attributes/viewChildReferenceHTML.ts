import {Component, ElementRef, ViewChild} from '@angular/core';

@Component({
    selector: 'app-test',
    templateUrl: "./viewChildReferenceHTML.html"
})
export class TestComponent {

    @ViewChild('ar<caret>ea') area: ElementRef;

}
