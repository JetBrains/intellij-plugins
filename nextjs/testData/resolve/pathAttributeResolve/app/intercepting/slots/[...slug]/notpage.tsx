export default function slotsTest({ params }: { params: { slug: string } }) {
  return <h1>Slots for {params.slug}</h1>;
}
