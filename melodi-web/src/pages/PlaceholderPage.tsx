type Props = { title: string }

export function PlaceholderPage({ title }: Props) {
  return (
    <div className="ui-surface rounded-xl border border-border/60 px-6 py-10 text-center">
      <h1 className="font-heading text-2xl font-semibold tracking-tight">{title}</h1>
      <p className="mt-2 text-sm text-muted-foreground">Placeholder</p>
    </div>
  )
}
