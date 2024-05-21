import {CodeMapping, VirtualCode} from "@volar/language-core"
import * as ts from "typescript";
import {IScriptSnapshot} from "typescript";

export class AngularVirtualCode implements VirtualCode {

  public snapshot: IScriptSnapshot = {
    getText: (start, end) => "",
    getLength: () => 0,
    getChangeRange(oldSnapshot) {
      return {
        span: {
          start: 0,
          length: oldSnapshot.getLength(),
        },
        newLength: 0
      };
    },
  };
  public mappings: CodeMapping[] = []
  public id: string = "main"

  constructor(
    public languageId: string,
  ) {
  }

  public sourceCode: string = ""

  checkUpdate(snapshot: ts.IScriptSnapshot, languageId?: string): AngularVirtualCode {
    if (snapshot.getText(0, snapshot.getLength()) !== this.sourceCode) {
      throw new Error("AngularVirtualCode source code out of sync.")
    }
    if (languageId !== undefined && languageId !== this.languageId) {
      throw new Error(`AngularVirtualCode languageId out of sync - expected ${languageId} by it is ${this.languageId}.`)
    }
    return this
  }

  update(sourceCode: string | undefined, transpiledCode: string | undefined, mappings: CodeMapping[]) {
    this.sourceCode = sourceCode ?? ""
    this.mappings = mappings
    const changeRanges = new Map<ts.IScriptSnapshot, ts.TextChangeRange | undefined>();
    this.snapshot = {
      getText: (start, end) => (transpiledCode ?? "").slice(start, end),
      getLength: () => (transpiledCode ?? "").length,
      getChangeRange(oldSnapshot) {
        if (!changeRanges.has(oldSnapshot)) {
          changeRanges.set(oldSnapshot, undefined);
          const oldText = oldSnapshot.getText(0, oldSnapshot.getLength());
          const changeRange = fullDiffTextChangeRange(oldText, (transpiledCode ?? ""));
          if (changeRange) {
            changeRanges.set(oldSnapshot, changeRange);
          }
        }
        return changeRanges.get(oldSnapshot);
      },
    };
  }
}

function fullDiffTextChangeRange(oldText: string, newText: string): ts.TextChangeRange | undefined {
  for (let start = 0; start < oldText.length && start < newText.length; start++) {
    if (oldText[start] !== newText[start]) {
      let end = oldText.length;
      for (let i = 0; i < oldText.length - start && i < newText.length - start; i++) {
        if (oldText[oldText.length - i - 1] !== newText[newText.length - i - 1]) {
          break;
        }
        end--;
      }
      let length = end - start;
      let newLength = length + (newText.length - oldText.length);
      if (newLength < 0) {
        length -= newLength;
        newLength = 0;
      }
      return {
        span: {start, length},
        newLength,
      };
    }
  }
  return undefined
}