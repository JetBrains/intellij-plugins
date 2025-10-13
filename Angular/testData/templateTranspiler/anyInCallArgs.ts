import {Component} from '@angular/core';

@Component({
  template: `
    <button (click)="newTask($any(input.value), $any(input.value), $any(input.value))">save</button>
  `,
})
export class NewTaskComponent {
  input: any;
  newTask(title: string, description: string, project: string) {}
}
