export interface IEvoControlError {
    [error: string]: string;
}
export declare class EvoControlErrorComponent {
    errors: any;
    errorsMessages: IEvoControlError;
    showCount: number;
    private defaultErrorMessages;
    readonly errorsMap: string[];
    showError(index: number): boolean;
}
