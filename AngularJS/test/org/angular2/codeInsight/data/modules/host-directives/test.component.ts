import { Component, OnInit } from '@angular/core';
import {MouseenterDirective} from "./directives/mouseenter.directive";

@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.css'],
  hostDirectives: [MouseenterDirective ]
})
export class SourceComponent implements OnInit {


  constructor() { }

  ngOnInit(): void {
  }

}
