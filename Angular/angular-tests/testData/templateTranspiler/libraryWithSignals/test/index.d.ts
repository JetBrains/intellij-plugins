import * as _angular_core from '@angular/core';

declare class Test {
    modelSignal: _angular_core.ModelSignal<string>;
    inputSignal: _angular_core.InputSignal<string>;
    outputSignal: _angular_core.OutputEmitterRef<void>;
    static ɵfac: _angular_core.ɵɵFactoryDeclaration<Test, never>;
    static ɵcmp: _angular_core.ɵɵComponentDeclaration<Test, "lib-test", never, { "modelSignal": { "alias": "modelSignal"; "required": false; "isSignal": true; }; "inputSignal": { "alias": "inputSignal"; "required": false; "isSignal": true; }; }, { "modelSignal": "modelSignalChange"; "outputSignal": "outputSignal"; }, never, never, true, never>;
}

export { Test };
