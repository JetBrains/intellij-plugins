import {Component} from "@angular/core";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-test',
  standalone: true,
  imports: [NgIf, NgForOf],
  templateUrl: "./externalTemplate.html",
})
export class TestComponent {

  items = [1, 2, 3];

}
