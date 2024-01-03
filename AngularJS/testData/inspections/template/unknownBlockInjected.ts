import {Component} from "@angular/core";

@Component({
   selector: 'app-test',
   standalone: true,
   template: `the <error descr="Unknown block @unknown block">@unknown block</error><error descr="Incomplete block - missing {">`</error>,
})
export class TestComponent {
}
