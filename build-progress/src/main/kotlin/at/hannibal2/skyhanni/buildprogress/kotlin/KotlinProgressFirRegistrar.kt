package at.hannibal2.skyhanni.buildprogress.kotlin

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class KotlinProgressFirRegistrar(private val tracker: KotlinProgressTracker) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +KotlinProgressCheckersExtension.Factory(tracker)
    }
}

class KotlinProgressCheckersExtension(session: FirSession, tracker: KotlinProgressTracker) : FirAdditionalCheckersExtension(session) {

    override val declarationCheckers = object : DeclarationCheckers() {
        override val fileCheckers = setOf(KotlinProgressFileChecker(tracker))
    }

    class Factory(private val tracker: KotlinProgressTracker) : FirAdditionalCheckersExtension.Factory {
        override fun create(session: FirSession) = KotlinProgressCheckersExtension(session, tracker)
    }
}

class KotlinProgressFileChecker(private val tracker: KotlinProgressTracker) : FirDeclarationChecker<FirFile>(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFile) = tracker.fileChecked(declaration.name)
}
