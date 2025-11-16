export function renderMarkdown(markdown: string): string {
  if (!markdown) return ''
  const escaped = markdown
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
  const paragraphs = escaped.split(/\n{2,}/).map((paragraph) => `<p>${paragraph.replace(/\n/g, '<br/>')}</p>`)
  return paragraphs.join('')
}

