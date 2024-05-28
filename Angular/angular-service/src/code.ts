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
  public source: { [key: string]: string } = {}

  public originalSourceSnapshot: IScriptSnapshot = this.snapshot

  constructor(private fileName: string) {
  }

  get languageId(): string {
    return "typescript"
  }

  sourceFileUpdated(snapshot: ts.IScriptSnapshot, _languageId?: string): AngularVirtualCode {
    if (this.snapshot.getLength() >= snapshot.getLength()
      && this.snapshot.getText(0, snapshot.getLength()) === snapshot.getText(0, snapshot.getLength())) {
      return this;
    }
    this.originalSourceSnapshot = snapshot

    this.source = {
      [this.fileName]: snapshot.getText(0, snapshot.getLength())
    }

    this.snapshot = snapshot
    this.mappings = [{
      source: this.fileName,
      generatedOffsets: [0],
      sourceOffsets: [0],
      lengths: [snapshot.getLength()],
      data: {
        format: true,
        completion: true,
        navigation: true,
        semantic: true,
        structure: true,
        verification: true,
      }
    }]
    return this
  }

  transpiledTemplateUpdated(transpiledCode: string | undefined, sourceCode: { [key: string]: string }, mappings: CodeMapping[]) {
    this.mappings = mappings.map(mapping => ({
      source: mapping.source,
      sourceOffsets: mapping.sourceOffsets,
      lengths: mapping.lengths,
      generatedOffsets: mapping.generatedOffsets,
      generatedLengths: mapping.generatedLengths,
      data: {
        format: mapping.source === this.fileName,
        completion: true,
        navigation: true,
        semantic: true,
        structure: true,
        verification: mapping.source === this.fileName,
      }
    }))
    this.source = {}
    for (const fileName in sourceCode) {
      this.source[fileName] = sourceCode[fileName]
    }
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