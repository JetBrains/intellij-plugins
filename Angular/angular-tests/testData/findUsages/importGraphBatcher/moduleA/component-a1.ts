import { Component } from '@angular/core';
import { ComponentC } from '../moduleC/component-c';

@Component({
  selector: 'a1',
  templateUrl: './component-a1.html',
})
export class ComponentA1 {
  c?: ComponentC;
}
