import { Component, OnInit } from '@angular/core';
import {Person} from "../Person";

@Component({
  selector: 'app-child',
  templateUrl: './child.component.html',
  styleUrls: ['./child.component.css']
})
export class ChildComponent implements OnInit {

    @Input() section: any;

    @Input() inputElement: HTMLInputElement;

    constructor() { }

  ngOnInit(): void {
  }

}