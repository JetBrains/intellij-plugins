import {Pipe} from "angular2/core";

@Pipe({
    name: "filta"
})
export class SearchPipe{
    transform(value){
        return value.filter((item)=> item.title.startsWith('s'));
    }
}