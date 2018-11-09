// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Pipe} from "angular2/core";

@Pipe({
    name: "filta"
})
export class SearchPipe{
    transform(value){
        return value.filter((item)=> item.title.startsWith('s'));
    }
}