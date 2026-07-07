import { Component } from '@angular/core';
import { ComponentA1 } from '../moduleA/component-a1';

@Component({
  selector: 'd',
  templateUrl: './component-d.html',
})
export class ComponentD {
  a?: ComponentA1;
}
