import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <div <weak_warning descr="No directive is matched on attribute matRowDef">*matRowDef</weak_warning>="let _row;  columns: displayedColumns; index as <warning descr="Unused constant i"><weak_warning descr="TS6133: 'i' is declared but its value is never read.">i</weak_warning></warning>; last as _last"></div>
  `,
})
export class AppComponent {
}
