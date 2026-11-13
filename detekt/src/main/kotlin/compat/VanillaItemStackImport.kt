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

    private val vanilla = SkyHanniFqNames.VANILLA_ITEM_STACK
    private val safe = SkyHanniFqNames.SAFE_ITEM_STACK

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        if (file.packageFqName == safe.packageFqName && file.declarations.any { it.name == safe.shortClassName.asString() }) return
        val directive = file.importDirectives.firstOrNull { it.importedFqName == vanilla.asSingleFqName() } ?: return
        (directive.importedReference ?: directive).reportIssue(
            "Direct import of `${vanilla.asFqNameString()}` is forbidden. Use `${safe.asFqNameString()}` instead.",
            directive,
        )
        if (directive.aliasName == null) correct(file, directive)
    }

    private fun correct(file: KtFile, directive: KtImportDirective) {
        if (!canCorrect(directive)) return
        val hasSafeImport = file.importDirectives.any { it.importedFqName == safe.asSingleFqName() }
        if (hasSafeImport) directive.deleteText() else directive.importedReference?.replaceText(safe.asFqNameString())
        file.collectDescendantsOfType<KtNameReferenceExpression> { it.isUnqualifiedItemStack() }.forEach {
            it.replaceText(safe.shortClassName.asString())
        }
    }

    private fun KtNameReferenceExpression.isUnqualifiedItemStack(): Boolean {
        if (getReferencedNameAsName() != vanilla.shortClassName) return false
        if (getParentOfType<KtImportDirective>(true) != null || getParentOfType<KtPackageDirective>(true) != null) return false
        return when (val parent = parent) {
            is KtUserType -> parent.qualifier == null
            is KtQualifiedExpression -> parent.receiverExpression == this
            is KtCallableReferenceExpression -> parent.receiverExpression == null || parent.receiverExpression == this
            else -> true
        }
    }
}
