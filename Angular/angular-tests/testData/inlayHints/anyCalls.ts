import {Component} from '@angular/core';

@Component(<hint text="obj(59120,59123):"/>{
  template: `
    <button (click)="newTask(<hint text="title(234,239):"/>$any(input.value), <hint text="description(249,260):"/>$any(input.value), <hint text="project(270,277):"/>$any(input.value))">save</button>
  `,
})
export class NewTaskComponent {
  input: any;
  newTask(title: string, description: string, project: string)<hint text=": void"/> {}
}
