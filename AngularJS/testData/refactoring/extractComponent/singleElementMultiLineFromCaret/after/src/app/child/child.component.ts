import {Component, OnInit} from '@angular/core';
import {Person} from "../Person";

@Component({
    selector: 'app-child',
    templateUrl: './child.component.html',
    styleUrls: ['./child.component.css']
})
export class ChildComponent implements OnInit {

    @Input() anotherText: string;

    @Input() handleEvent: (event?: Event) => void;

    @Input() titleContent: string;

    @Input() section: any;

    @Input() inputElement: HTMLInputElement;

    @Input() dedicatedTextInterpolation: string;

    @Input() examplePerson: Person;

    @Input() twoWay: number;

    @Output() twoWayChange = new EventEmitter<number>();

    @Input() actions: string[];

    @Input() actionPrefix: string;

    constructor() {
    }

    ngOnInit(): void {
    }

}