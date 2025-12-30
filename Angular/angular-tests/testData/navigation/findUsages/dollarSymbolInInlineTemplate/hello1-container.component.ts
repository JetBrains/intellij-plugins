import {Component} from '@angular/core';
import {Hello1Service} from './hello1.service';

@Component({
  selector: 'app-hello1-container',
  template: `
    <div [title]="hello1Service.abc$"></div>
  `,
  styles: []
})
export class Hello1ContainerComponent {
  constructor(public hello1Service: Hello1Service) {
  }
}
