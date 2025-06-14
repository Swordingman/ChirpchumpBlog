import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-dark.css';

const md = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: true,
    highlight: function (str, lang) {
        if (lang && hljs.getLanguage(lang)) {
            try {
                const highlightedCode = hljs.highlight(str, {
                    language: lang,
                    ignoreIllegals: true
                }).value;

                return `<pre class="hljs"><code>${highlightedCode}</code></pre>`;
            } catch (__) {}
        }

        return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`;
    }
});

export function renderMarkdown(markdownText) {
    if (!markdownText) return '';
    return md.render(markdownText);
}

export default md;