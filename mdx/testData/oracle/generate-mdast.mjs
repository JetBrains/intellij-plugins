#!/usr/bin/env node
// Offline mdast ORACLE generator for the MDX plugin's parsing fixtures.
//
// Compiles each `.mdx` fixture with the real MDX remark pipeline
// (remark-parse + remark-gfm + remark-frontmatter + remark-mdx) and writes a
// normalized, deterministic mdast skeleton to `<Name>.mdast.json`.
//
// This is a *reference oracle*: it captures what the canonical MDX toolchain
// (the same one mdxjs.com uses) parses each fixture into, so Kotlin tests and
// reviewers can diff against a real AST instead of trusting hand-written prose.
//
// Usage:
//   node oracle/generate-mdast.mjs [--in <dir>] [--out <dir>] [--positions]
//
//   --in <dir>    Directory to scan for *.mdx (default: parsing)
//   --out <dir>   Directory to write *.mdast.json (default: oracle)
//   --positions   Include unist `position` ranges (default: omitted for stability)
//
// Determinism: input files are sorted, object keys are sorted on output, the
// indentation is fixed (2 spaces) with a trailing LF, and no timestamps or
// absolute paths appear in the emitted JSON. Re-running over unchanged inputs
// produces a byte-identical result (no git diff).

import {unified} from 'unified'
import remarkParse from 'remark-parse'
import remarkGfm from 'remark-gfm'
import remarkFrontmatter from 'remark-frontmatter'
import remarkMdx from 'remark-mdx'
import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'

const SCRIPT_DIR = path.dirname(fileURLToPath(import.meta.url))
// testData/ is the parent of the oracle/ dir this script lives in.
const TEST_DATA_DIR = path.resolve(SCRIPT_DIR, '..')

// ---------------------------------------------------------------------------
// Argument parsing
// ---------------------------------------------------------------------------
function parseArgs(argv) {
  const opts = {in: 'parsing', out: 'oracle', positions: false}
  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i]
    if (arg === '--positions') {
      opts.positions = true
    } else if (arg === '--in') {
      if (i + 1 >= argv.length) throw new Error('Missing value for --in')
      opts.in = argv[++i]
    } else if (arg === '--out') {
      if (i + 1 >= argv.length) throw new Error('Missing value for --out')
      opts.out = argv[++i]
    } else if (arg.startsWith('--in=')) {
      opts.in = arg.slice('--in='.length)
    } else if (arg.startsWith('--out=')) {
      opts.out = arg.slice('--out='.length)
    } else {
      throw new Error(`Unknown argument: ${arg}`)
    }
  }
  if (!opts.in) throw new Error('Empty value for --in')
  if (!opts.out) throw new Error('Empty value for --out')
  return opts
}

// ---------------------------------------------------------------------------
// MDX remark pipeline (parse only, never advances to hast/estree).
//
// We deliberately build a *remark-only* unified processor instead of
// @mdx-js/mdx's createProcessor(): the latter registers the full compile chain
// (mdast -> hast -> estree -> JS), so runSync() would leave mdast behind. This
// processor's transformer chain stays at the mdast layer, so parse()+runSync()
// yields exactly the Markdown AST with MDX/GFM/frontmatter nodes as first-class
// members.
// ---------------------------------------------------------------------------
const processor = unified()
  .use(remarkParse)
  .use(remarkGfm)
  .use(remarkFrontmatter, ['yaml', 'toml'])
  .use(remarkMdx)

// hast node types that must never appear in an mdast tree. If we ever see one,
// the pipeline silently advanced past mdast and the oracle would be wrong.
const HAST_NODE_TYPES = new Set(['element', 'doctype'])

// ---------------------------------------------------------------------------
// Normalization
//
// Keep `type` plus a small, explicit whitelist of structural fields per node.
// Always drop `position` (unless --positions) and `data` (bulky estree etc.).
// ---------------------------------------------------------------------------

// Scalar/structural fields worth keeping, by mdast node type. `type` and
// `children` are handled separately and never listed here.
const FIELDS_BY_TYPE = {
  heading: ['depth'],
  code: ['lang', 'meta', 'value'],
  inlineCode: ['value'],
  text: ['value'],
  yaml: ['value'],
  toml: ['value'],
  html: ['value'],
  list: ['ordered', 'start', 'spread'],
  listItem: ['spread', 'checked'],
  link: ['url', 'title'],
  image: ['url', 'title', 'alt'],
  linkReference: ['identifier', 'label', 'referenceType'],
  imageReference: ['identifier', 'label', 'referenceType', 'alt'],
  definition: ['identifier', 'label', 'url', 'title'],
  footnoteReference: ['identifier', 'label'],
  footnoteDefinition: ['identifier', 'label'],
  table: ['align'],
  mdxjsEsm: ['value'],
  mdxFlowExpression: ['value'],
  mdxTextExpression: ['value'],
  mdxJsxFlowElement: ['name'],
  mdxJsxTextElement: ['name'],
}

// Normalize a single mdxJsxAttribute / mdxJsxExpressionAttribute entry.
function normalizeAttribute(attr) {
  if (attr == null || typeof attr !== 'object') return attr
  // Spread attribute: {type: 'mdxJsxExpressionAttribute', value: '...js...', data}
  if (attr.type === 'mdxJsxExpressionAttribute') {
    return {type: attr.type, value: attr.value}
  }
  // Named attribute: {type: 'mdxJsxAttribute', name, value}
  const out = {type: attr.type, name: attr.name}
  if (attr.value === null || attr.value === undefined) {
    // boolean-style attribute (e.g. `disabled`) -> value omitted/null
    out.value = null
  } else if (typeof attr.value === 'string') {
    out.value = attr.value
  } else if (typeof attr.value === 'object' && attr.value.type === 'mdxJsxAttributeValueExpression') {
    // Expression attribute: keep the raw `{...}` source text, drop estree.
    out.value = {type: attr.value.type, value: attr.value.value}
  } else {
    out.value = attr.value
  }
  return out
}

function normalizeNode(node, opts) {
  if (node == null || typeof node !== 'object') return node

  if (HAST_NODE_TYPES.has(node.type)) {
    throw new Error(
      `Unexpected hast node "${node.type}" in mdast tree: pipeline advanced past mdast`,
    )
  }

  const out = {type: node.type}

  const fields = FIELDS_BY_TYPE[node.type] || []
  for (const field of fields) {
    if (Object.prototype.hasOwnProperty.call(node, field) && node[field] !== undefined) {
      out[field] = node[field]
    }
  }

  // JSX elements carry an `attributes` array.
  if (Array.isArray(node.attributes)) {
    out.attributes = node.attributes.map(normalizeAttribute)
  }

  if (opts.positions && node.position) {
    out.position = node.position
  }

  if (Array.isArray(node.children)) {
    out.children = node.children.map((child) => normalizeNode(child, opts))
  }

  return out
}

// ---------------------------------------------------------------------------
// Deterministic JSON serialization (sorted keys, 2-space indent, trailing LF).
// ---------------------------------------------------------------------------
function sortKeysDeep(value) {
  if (Array.isArray(value)) return value.map(sortKeysDeep)
  if (value && typeof value === 'object') {
    const sorted = {}
    for (const key of Object.keys(value).sort()) {
      sorted[key] = sortKeysDeep(value[key])
    }
    return sorted
  }
  return value
}

function stableStringify(value) {
  return JSON.stringify(sortKeysDeep(value), null, 2) + '\n'
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------
function main() {
  const opts = parseArgs(process.argv.slice(2))
  const inDir = path.resolve(TEST_DATA_DIR, opts.in)
  const outDir = path.resolve(TEST_DATA_DIR, opts.out)

  if (!fs.existsSync(inDir) || !fs.statSync(inDir).isDirectory()) {
    console.error(`Input directory not found: ${inDir}`)
    process.exit(1)
  }
  fs.mkdirSync(outDir, {recursive: true})

  const fixtures = fs
    .readdirSync(inDir)
    .filter((name) => name.endsWith('.mdx'))
    .sort()

  let wrote = 0
  let unchanged = 0
  const errors = []

  for (const fileName of fixtures) {
    const baseName = fileName.slice(0, -'.mdx'.length)
    const inPath = path.join(inDir, fileName)
    const outPath = path.join(outDir, `${baseName}.mdast.json`)
    const source = fs.readFileSync(inPath, 'utf8')

    let payload
    try {
      const tree = processor.runSync(processor.parse(source))
      payload = {
        source: `${path.basename(inDir)}/${fileName}`,
        mdast: normalizeNode(tree, opts),
      }
    } catch (err) {
      const reason = err && err.reason ? err.reason : err && err.message ? err.message : String(err)
      const where =
        err && err.line != null && err.column != null ? ` (line ${err.line}, column ${err.column})` : ''
      const ruleId = err && err.ruleId ? err.ruleId : null
      errors.push({file: fileName, reason: `${reason}${where}`, ruleId})
      // The MDX toolchain *rejecting* a fixture is itself a meaningful oracle
      // result (e.g. autolinks `<https://...>` are not valid MDX). Record it.
      payload = {
        source: `${path.basename(inDir)}/${fileName}`,
        parseError: {reason, ruleId, line: err && err.line != null ? err.line : null, column: err && err.column != null ? err.column : null},
      }
    }

    // Write only when content actually changes: output is deterministic, so an
    // unchanged input yields byte-identical JSON. Skipping the write avoids
    // touching mtimes / triggering watchers on a no-op regeneration.
    const next = stableStringify(payload)
    const prev = fs.existsSync(outPath) ? fs.readFileSync(outPath, 'utf8') : null
    if (prev === next) {
      unchanged++
    } else {
      fs.writeFileSync(outPath, next)
      wrote++
    }
  }

  console.log('MDX mdast oracle generation')
  console.log(`  input dir : ${path.relative(TEST_DATA_DIR, inDir) || '.'}`)
  console.log(`  output dir: ${path.relative(TEST_DATA_DIR, outDir) || '.'}`)
  console.log(`  fixtures  : ${fixtures.length} processed, ${wrote} written, ${unchanged} unchanged`)
  if (errors.length === 0) {
    console.log('  parse errors: none')
  } else {
    console.log(`  parse errors: ${errors.length} (recorded as parseError in the oracle)`)
    for (const e of errors) {
      console.log(`    - ${e.file}: ${e.reason}${e.ruleId ? ` [${e.ruleId}]` : ''}`)
    }
  }
}

main()
