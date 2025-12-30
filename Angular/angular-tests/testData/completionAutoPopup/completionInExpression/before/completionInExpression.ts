import {Component, Input} from '@angular/core';

enum CdkColors {
  red, blue, orange
}

@Component({
             selector: 'app-component',
             template: `
                {{Cd<caret>}}
             `
           })
export class CdkAppComponent {
  @Input()
  CdkColor: CdkColors

  @Input()
  CdkValue: Number

}
