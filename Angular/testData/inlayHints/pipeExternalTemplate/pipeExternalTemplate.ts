import {Component} from "@angular/core";
import {PercentPipe} from "@angular/common";

@Component({
  selector: 'app-test',
  standalone: true,
  imports: [PercentPipe],
  templateUrl: "./pipeExternalTemplate.html",
})
export class TestComponent {

}
