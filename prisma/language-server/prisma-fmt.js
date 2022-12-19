import prismaFmt from '@prisma/prisma-fmt-wasm'

const EXIT_TIMEOUT = 10000

const formattingParams = process.argv[2]
let schema = ""

function formatSchema() {
  try {
    const formatted = prismaFmt.format(schema, formattingParams)
    console.log(formatted)
    process.exit(0)
  }
  catch (e) {
    console.error(e)
    process.exit(1)
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


function handleTimeout() {
  setTimeout(() => {
    process.exit(1)
  }, EXIT_TIMEOUT)
}

handleTimeout()