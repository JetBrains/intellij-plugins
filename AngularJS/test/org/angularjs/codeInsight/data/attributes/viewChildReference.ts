import {Component, ElementRef, ViewChild} from '@angular/core';

@Component({
    selector: 'app-test',
    template: `
      <textarea #area></textarea>
      <div #area2></div>
    `
})
export class TestComponent {

    @ViewChild('ar<caret>ea') area: ElementRef;

}
