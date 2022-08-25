// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core"

@Component({
    selector: 'my-customer',
    templateUrl: './custom.html',
    properties: {
        'id': 'dependency'
    }
})
class Dependency {
    id: string;
}

<structure>
<node text="Structure.ts">
  <node text="Dependency">
    <node text="id: string" icon="Field"/>
  </node>
</node>
</structure>
