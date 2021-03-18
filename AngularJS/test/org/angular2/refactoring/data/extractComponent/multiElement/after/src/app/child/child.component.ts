import { Component, OnInit } from '@angular/core';
import {Person} from "../Person";

@Component({
  selector: 'app-child',
  templateUrl: './child.component.html',
  styleUrls: ['./child.component.css']
})
export class ChildComponent implements OnInit {

    @Input() titleContent: string;

    @Input() handleEvent: (event?: Event) => void;

    @Input() section: any;

    @Input() inputElement: HTMLInputElement;

    @Input() anotherText: string;

    @Input() dedicatedTextInterpolation: string;

    constructor() { }

  ngOnInit(): void {
  }

}