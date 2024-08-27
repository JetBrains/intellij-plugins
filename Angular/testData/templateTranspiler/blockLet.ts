import {Component} from '@angular/core';
import {NgIf} from '@angular/common';

@Component({
 selector: 'let-test',
 standalone: true,
 imports: [NgIf],
 template: `
    @let topLevel = value;
    <div>
      @let insideDiv = value;
    </div>
    {{topLevel}} <!-- Valid -->
    {{insideDiv}} <!-- Valid -->
    @if (condition) {
      {{topLevel + insideDiv}} <!-- Valid -->
      @let nested = value;
      @if (condition) {
        {{topLevel + insideDiv + nested}} <!-- Valid -->
      }
    }
    <div *ngIf="condition">
      {{topLevel + insideDiv}} <!-- Valid -->
      @let nestedNgIf = value;
      <div *ngIf="condition">
         {{topLevel + insideDiv + nestedNgIf}} <!-- Valid -->
      </div>
    </div>
    {{nested}} <!-- Error, not hoisted from @if -->
    {{nestedNgIf}} <!-- Error, not hoisted from *ngIf -->
  `
})
export class LetComponent {
  value!: string
}
