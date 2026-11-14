package compat

import SkyHanniRule
import dev.detekt.api.Config
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import utils.SkyHanniFqNames

class VanillaItemStackImport(config: Config) : SkyHanniRule(
    config,
    "Use SafeItemStack instead of the vanilla ItemStack import to avoid 'Components not bound yet' crashes.",
) {

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        if (file.name.endsWith("SafeItemStack.kt")) return
        val directive = file.importDirectives.firstOrNull { it.importedFqName?.asString() == SkyHanniFqNames.VANILLA_ITEM_STACK } ?: return
        (directive.importedReference ?: directive).reportIssue(
            "Direct import of `${SkyHanniFqNames.VANILLA_ITEM_STACK}` is forbidden. Use `${SkyHanniFqNames.SAFE_ITEM_STACK}` instead.",
            directive,
        )
        if (directive.aliasName == null) correct(file, directive)
    }

    private fun correct(file: KtFile, directive: KtImportDirective) {
        if (!canCorrect(directive)) return
        val hasSafeImport = file.importDirectives.any { it.importedFqName?.asString() == SkyHanniFqNames.SAFE_ITEM_STACK }
        if (hasSafeImport) directive.deleteText() else directive.importedReference?.replaceText(SkyHanniFqNames.SAFE_ITEM_STACK)
        file.collectDescendantsOfType<KtNameReferenceExpression> { it.isUnqualifiedItemStack() }.forEach {
            it.replaceText(SAFE_NAME)
        }
    }

    private fun KtNameReferenceExpression.isUnqualifiedItemStack(): Boolean {
        if (getReferencedName() != VANILLA_NAME) return false
        if (getParentOfType<KtImportDirective>(true) != null || getParentOfType<KtPackageDirective>(true) != null) return false
        return when (val parent = parent) {
            is KtUserType -> parent.qualifier == null
            is KtQualifiedExpression -> parent.receiverExpression == this
            is KtCallableReferenceExpression -> parent.receiverExpression == null || parent.receiverExpression == this
            else -> true
        }
    }

    companion object {
        private const val VANILLA_NAME = "ItemStack"
        private const val SAFE_NAME = "SafeItemStack"
    }
}
