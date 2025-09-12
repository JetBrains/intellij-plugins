import { Component, OnInit, signal } from '@angular/core';
import { FlexRenderDirective } from './flex-render';

@Component({
  selector: 'app-root',
  imports: [FlexRenderDirective],
  template: `
    <table>
      <thead>
        @for (headerGroup of table.getHeaderGroups(); track headerGroup.id) {
          <tr>
            @for (header of headerGroup.headers; track header.id) {
              <th>
                <ng-container
                  *flexRender="
                        header.column.columnDef.header;
                        props: header.getContext();
                        let header
                      "
                >
                  <div [innerHTML]="header"></div>
                </ng-container>
              </th>
            }
          </tr>
        }
      </thead>
      <tbody></tbody>
    </table>
  `
})
export class App implements OnInit {
  data = signal("");

  table = signal(null);

  ngOnInit() {
    console.log('test');
    const arr = [1, 2, 3];
    arr.includes(2);
  }
}
