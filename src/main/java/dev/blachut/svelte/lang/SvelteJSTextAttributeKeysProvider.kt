package dev.blachut.svelte.lang

import com.intellij.lang.javascript.highlighting.DefaultJSTextAttributeKeysProvider
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lang.javascript.highlighting.JSSemanticHighlightingUtil
import com.intellij.lang.javascript.highlighting.JSTextAttributeKeysProvider
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute

@Suppress("UsePropertyAccessSyntax")
class SvelteJSTextAttributeKeysProvider : JSTextAttributeKeysProvider() {
    private val delegate = DefaultJSTextAttributeKeysProvider.getInstance()

    override fun getTextAttributeKeyInfoForResolveResult(
        resolve: PsiElement,
        highlighter: JSHighlighter?
    ): JSSemanticHighlightingUtil.TextAttributeKeyInfo? {
        if (resolve is SvelteHtmlAttribute.ShorthandLetImplicitElement) {
            return JSSemanticHighlightingUtil.TextAttributeKeyInfo.parameter(highlighter, delegate)
        }

        return delegate.getTextAttributeKeyInfoForResolveResult(resolve, highlighter)
    }

    override fun getTextAttributesKeyForClass() = delegate.getTextAttributesKeyForClass()

    override fun getTextAttributesKeyForInstanceMethod() = delegate.getTextAttributesKeyForInstanceMethod()

    override fun getTextAttributesKeyForInstanceField() = delegate.getTextAttributesKeyForInstanceField()

    override fun getAttributesKeyForLocalVariable(): TextAttributesKey = delegate.getAttributesKeyForLocalVariable()

    override fun getAttributesKeyForGlobalVariable() = delegate.getAttributesKeyForGlobalVariable()

    override fun getAttributesKeyForParameter() = delegate.getAttributesKeyForParameter()
}
