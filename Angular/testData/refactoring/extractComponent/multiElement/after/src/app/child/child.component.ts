import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'app-child',
    templateUrl: './child.component.html',
    styleUrls: ['./child.component.css']
})
export class ChildComponent implements OnInit {

    @Input() titleContent: string;

    @Input() handleEvent: (event?: Event) => void;

    @Input() section: number;

    @Input() inputElement: HTMLInputElement;

    @Input() anotherText: string;

    @Input() dedicatedTextInterpolation: string;

    constructor() {
    }

    ngOnInit(): void {
    }

}