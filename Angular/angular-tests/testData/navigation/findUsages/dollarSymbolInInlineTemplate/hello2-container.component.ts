import {Component} from '@angular/core';
import {Hello1Service} from './hello1.service';

@Component({
  selector: 'app-hello2-container',
  template: `
    <div [title]="hello1Service.abc$"></div>
  `,
  styles: []
})
export class Hello2ContainerComponent {
  constructor(public hello1Service: Hello1Service) {
    hello1Service.abc$
  }
}
