import {Component, Input} from '@angular/core';
import {CdkThemes} from "./cdkThemes";

enum CdkColors {
  red, blue, orange
}

@Component({
             selector: 'app-component',
             template: `
                {{CdkThemes.light;CdkColors.red;Math.abs()}}
             `
           })
export class CdkAppComponent {
  @Input()
  CdkColor: CdkColors

  @Input()
  CdkValue: Number

    protected readonly CdkThemes = CdkThemes;
    protected readonly CdkColors = CdkColors;
    protected readonly Math = Math;
}
