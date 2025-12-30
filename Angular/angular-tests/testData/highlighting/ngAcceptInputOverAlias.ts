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
            <error descr="TS2322: Type 'string' is not assignable to type 'number'."><warning descr="cdkConnectedOverlayViewportMargin requires value">cdkConnectedOverlayViewportMargin</warning></error>
            <error descr="TS2322: Type 'boolean' is not assignable to type 'string | number'.">[cdkConnectedOverlayMinWidth]</error>="true"
            cdkConnectedOverlayHasBackdrop
            [cdkConnectedOverlayHasBackdrop]="12"
        >
            <!-- ... -->
        </ng-template>
    `
})
export class TestComponent {
}
