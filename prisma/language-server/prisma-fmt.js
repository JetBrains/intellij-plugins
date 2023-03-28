import prismaFmt from '@prisma/prisma-fmt-wasm'

const EXIT_TIMEOUT = 10000

const formattingParams = process.argv[2]

let schema = ""
let timeoutId

function formatSchema() {
  try {
    const formatted = prismaFmt.format(schema, formattingParams)
    console.log(formatted)
  }
  catch (e) {
    console.error(e)
    process.exit(1)
  }
  finally {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
  }
}

const input = process.stdin
input.setEncoding('utf8')
input.on('readable', () => {
  let chunk
  while ((chunk = input.read()) !== null) {
    schema += chunk
  }
})
input.on('end', () => {
  formatSchema()
})

timeoutId = setTimeout(() => {
  process.exit(1)
}, EXIT_TIMEOUT)