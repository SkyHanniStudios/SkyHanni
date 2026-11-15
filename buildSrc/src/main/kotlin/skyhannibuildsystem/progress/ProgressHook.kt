package skyhannibuildsystem.progress

interface ProgressHook {
    val id: String
}

enum class KotlinHook(override val id: String) : ProgressHook {
    REGISTER("CompilerPluginRegistrar.registerExtensions"),
    STATUS("FirStatusTransformerExtension.needTransformStatus"),
    CHECK("FirDeclarationChecker.check"),
    IR("IrGenerationExtension.generate"),
    CODEGEN("ClassGeneratorExtension.generateClass"),
}

enum class JavacHook(override val id: String) : ProgressHook {
    INIT("Plugin.init"),
    PARSE("TaskEvent.Kind.PARSE"),
    ENTER("TaskEvent.Kind.ENTER"),
    ANALYZE("TaskEvent.Kind.ANALYZE"),
    GENERATE("TaskEvent.Kind.GENERATE"),
    COMPILATION("TaskEvent.Kind.COMPILATION"),
}

enum class KspHook(override val id: String) : ProgressHook {
    CREATE("SymbolProcessorProvider.create"),
    PROCESS("SymbolProcessor.process"),
    FINISH("SymbolProcessor.finish"),
}

enum class DetektHook(override val id: String) : ProgressHook {
    INIT("FileProcessListener.init"),
    START("FileProcessListener.onStart"),
    PROCESS_COMPLETE("FileProcessListener.onProcessComplete"),
    FINISH("FileProcessListener.onFinish"),
}
