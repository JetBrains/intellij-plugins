import {Component} from "@angular/core";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-test',
  standalone: true,
  imports: [NgIf, NgForOf],
  template: `
    <div *ngFor="let someVar<hint text=": number"/> of items">
      @let anotherVar<hint text=": number"/> = someVar * 2;
      <span>
            {{ anotherVar }}
          </span>
    </div>
  `,
})
export class TestComponent {

  items<hint text=": number[]"/> = [1, 2, 3];

}
