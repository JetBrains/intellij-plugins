export const dynamic = 'auto'
export const dynamicParams = true
export const revalidate = false
export const fetchCache = 'auto'
export const runtime = 'nodejs'
export const preferredRegion = 'auto'
export const maxDuration = 5

export const metadata = {
  title: '...',
}

export async function generateMetadata() {
  return {
    title: '...',
  }
}

export const viewport = {
  themeColor: 'black',
}

export function generateViewport() {
  return {
    themeColor: '...',
  }
}

export function generateStaticParams() {
  return [{ id: '1' }, { id: '2' }, { id: '3' }]
}

export async function generateImageMetadata() {}

export default function Layout() {}
