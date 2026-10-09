import {Component} from '@angular/core';

@Component({
  selector: 'in-element-comments',
  standalone: true,
  template: `
    <div
      // [title]="missing"
      [title]="title"
      /* (click)="missingHandler()" */ (click)="onClick()"
      /*
        [id]="missingId"
      */
      class="foo"
    ></div>
    <input // comment before the tag end
    >
    <div [title]="<error descr="TS2339: Property 'missingTitle' does not exist on type 'InElementCommentsComponent'.">missingTitle</error>"></div>
  `
})
export class InElementCommentsComponent {
  title = 'title';

  onClick() {
  }
}
