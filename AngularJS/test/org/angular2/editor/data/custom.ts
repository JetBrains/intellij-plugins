// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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