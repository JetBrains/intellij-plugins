import {Component, OnInit} from '@angular/core';

@Component({
    selector: 'app-child',
    templateUrl: './child.component.html',
    styleUrls: ['./child.component.css']
})
export class ChildComponent implements OnInit {

    @Input() twoWay: boolean;

    @Input() twoWay_1: number;

    @Output() twoWay_1Change = new EventEmitter<number>();

    @Input() twoWay_2: number;

    @Output() twoWay_2Change = new EventEmitter<number>();

    constructor() {
    }

    ngOnInit(): void {
    }

}