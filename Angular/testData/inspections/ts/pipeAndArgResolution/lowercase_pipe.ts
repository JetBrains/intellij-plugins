import {Pipe, PipeTransform} from '@angular/core';

@Pipe({ name: "lowercase"})
export declare class LowercasePipe implements PipeTransform {
    transform(value: string): string {
        return value;
    }
}
