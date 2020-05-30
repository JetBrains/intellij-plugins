// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
    <node text="id: string" icon="FIELD_ICON"/>
  </node>
</node>
</structure>
