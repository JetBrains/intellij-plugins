import {Component} from '@angular/core';

@Component({
  selector: 'regex-test',
  standalone: true,
  template: `
    <!-- should report invalid regular expression flag -->
    {{ "foo".match(/abc/<error descr=", or ) expected">O</error><error descr="Unexpected token ')'">)</error> }}
    
    <!-- should report unterminated regex -->
    {{ <error descr="TS1161: Unterminated regular expression literal.">/abc</error> }}
    
    <!-- should not report errors on properly escaped regex -->
    {{ /^http:\\/\\/foo\\.bar/.test(value) }}
  `
})
export class RegexComponent {
    value!: string
}
