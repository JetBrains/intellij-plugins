// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CdkConnectedOverlay} from "@angular/cdk/overlay";

@Component({
   standalone: true,
   imports: [
     CdkConnectedOverlay
   ],
   template: `
        <ng-template
            cdkConnectedOverlay
            cdkConnectedOverlayFlexibleDimensions="true"
            <error descr="Type \"\" is not assignable to type number">cdkConnectedOverlayViewportMargin</error>
            [cdkConnectedOverlayMinWidth]="<error descr="Type boolean is not assignable to type number | string  Type boolean is not assignable to type string    Type boolean is not assignable to type number">true</error>"
            cdkConnectedOverlayHasBackdrop
            [cdkConnectedOverlayHasBackdrop]="12"
        >
            <!-- ... -->
        </ng-template>
    `
})
export class TestComponent {
}
