import {Component,} from '@angular/core';
import {NgForOf} from "@angular/common";

@Component({
  selector: 'test',
  standalone: true,
  template: `
        <div *ngFor="let foo of [1,2,3]; trackBy: trackByFn; let index = index">{{ foo + index }}</div>
        <div *ngFor="<error descr="Expression expected">;</error> of [1,2,3] as foo;">{{ foo }}</div>
        <div *ngFor="let foo of [1,2,3]; <error descr="Property ngForTrckBy is not provided by any applicable directive on an embedded template">trckBy</error>: trackByFn">{{ foo }}</div>
        <div <error descr="Missing binding for required input ngForOf of directive NgForOf">*ngFor</error>="let foo <error descr="Property ngForOff is not provided by any applicable directive on an embedded template">off</error> [1,2,3]">{{ foo }}</div>
        <div *ngFor="let foo of [1,2,3]; let index = <error descr="TS2551: Property 'indx' does not exist on type 'NgForOfContext<number, number[]>'. Did you mean 'index'?">indx</error>">{{ foo + index }}</div>
        <div *ngFor="let foo of [1,2,3]; trackBy: trackByFn as <error descr="TS2339: Property 'ngForTrackBy' does not exist on type 'NgForOfContext<number, number[]>'.">check</error>">{{ foo + check }}</div>
        <div *ngFor="let foo of [1,2,3]; <error descr="TS2322: Type '(_$event: string) => void' is not assignable to type 'TrackByFunction<number>'.
  Types of parameters '_$event' and 'index' are incompatible.
    Type 'number' is not assignable to type 'string'.">trackBy</error>: onKeyDown">{{ foo }}</div>
        <ng-template #foo></ng-template>
    `,
  imports: [
    NgForOf
  ]
})
export class ChipComponent {

  onKeyDown(_$event: string) {
  }

  trackByFn(index: number, _item: number) {
    return index;
  }
}

