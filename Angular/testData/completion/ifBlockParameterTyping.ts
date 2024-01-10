import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';


@Component({
             selector: 'human-profile',
             standalone: true,
             imports: [CommonModule],
             template: `
    @if (user.kind; <caret>)
  `,
             styles: []
           })
export class HumanProfileComponent {

}
