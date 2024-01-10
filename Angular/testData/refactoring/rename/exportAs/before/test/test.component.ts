import {Component, OnInit, Input} from '@angular/core';
import {MouseenterDirective} from "../directives/mouseenter.directive";

@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  exportAs: "bold",
  standalone: true,
  hostDirectives: [MouseenterDirective ]
})
export class TestComponent implements OnInit {

  @Input()
  bold: Boolean = false

}
