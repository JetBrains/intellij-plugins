import { combineLatest, map, of } from "rxjs";

type User = Readonly<{
    id: string;
    name: string;
}>;
type Test = Readonly<{
    a: string;
    b: number;
    c: boolean;
}>;

const user$ = of({} as User);
const test$ = of({} as Test);

combineLatest([user$, test$]).pipe(
    map(([user, test]) => ({
        userId: user.i<caret>d,
        testA: test.a
    }))
);
