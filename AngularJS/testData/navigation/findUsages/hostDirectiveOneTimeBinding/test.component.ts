import { Component, OnInit } from '@angular/core';
import {MouseenterDirective} from "./mouseenter.directive";

@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  hostDirectives: [MouseenterDirective]
})
export class TestComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
