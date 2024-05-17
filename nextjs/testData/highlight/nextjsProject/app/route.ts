export const dynamic = 'auto'
export const dynamicParams = true
export const revalidate = false
export const fetchCache = 'auto'
export const runtime = 'nodejs'
export const preferredRegion = 'auto'
export const maxDuration = 5

export function generateStaticParams() {
  return [{ id: '1' }, { id: '2' }, { id: '3' }]
}

export async function generateImageMetadata() {}

export async function GET() {}
export async function HEAD() {}
export async function POST() {}
export async function PUT() {}
export async function DELETE() {}
export async function PATCH() {}
export async function OPTIONS() {}
