// src/utils/markdown.js
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
// 引入一个你喜欢的高亮主题样式
import 'highlight.js/styles/atom-one-dark.css'; // 我个人很喜欢这个暗色主题，你也可以换成其他的，比如 'github.css'

const md = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: true,
    highlight: function (str, lang) {
        // str:  代码块的原始内容
        // lang: 代码块指定的语言，例如 'javascript', 'java'

        // 1. 如果指定了语言，并且 highlight.js 支持该语言
        if (lang && hljs.getLanguage(lang)) {
            try {
                // 2. 调用 highlight.js 进行高亮处理
                const highlightedCode = hljs.highlight(str, {
                    language: lang,
                    ignoreIllegals: true // 忽略无法识别的语法，避免报错
                }).value;

                // 3. 返回最终的 HTML 结构，带有 hljs 的类名，以便 CSS 生效
                return `<pre class="hljs"><code>${highlightedCode}</code></pre>`;
            } catch (__) {}
        }

        // 4. 如果没有指定语言或不支持，则只进行 HTML 转义，防止 XSS
        return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`;
    }
});

// 导出一个渲染函数
export function renderMarkdown(markdownText) {
    if (!markdownText) return '';
    return md.render(markdownText);
}

// 也可以导出实例，如果其他地方需要
export default md;