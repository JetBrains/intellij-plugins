import {Component} from '@angular/core';

export interface User {
  name: string,
  pictureUrl: string,
  isHuman?: boolean,
  isRobot?: boolean,
}

@Component({
  selector: 'robot-profile',
  standalone: true,
  templateUrl: "./regexesExt.html",
  host: {
    "[small]": "/^http:\\/\\/foo\\.bar/.test(\"\\\"\")",
  },
})
export class RobotProfileComponent {
    user!: User
}
