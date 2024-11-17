export class FooCls {
  private singleParam: number;

  constructor();
  constructor(singleParam: number);
  constructor(singleParam?: number) {
    if (singleParam !== undefined) {
      this.singleParam = singleParam;
    } else {
      this.singleParam = 0;
    }
  }

  foo(): number {
    return 42;
  }

  bar(i: number): number {
    if (i % 2) {
      fun2();
      return 42;
    }
    return -1;
  }

  baz(i: number): number {
    if (i % 2) {
      return 42;
    }
    i++;
    i++;
    return -1;
  }

  foobar = (): number => {
    return 42;
  }

  noBody: () => void;
}

new class {
  foo(i: number) {
    (() => {
      console.log("Hello from an unassigned anonymous function inside an anonymous class!");
    })();
  }
};

function fun1(): void {
  console.log("Information about the environment");
}

export function fun2(): number {
  return 42;
}

export class BarCls {
  private singleParam: number;

  constructor();
  constructor(singleParam: number);
  constructor(singleParam?: number) {
    if (singleParam !== undefined) {
      this.singleParam = singleParam;
    } else {
      this.singleParam = 0;
    }
  }
}