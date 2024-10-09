package org.aurora.core;

import org.aurora.binary.AurBytecodeEmissionPass;
import org.aurora.compiler.AurCompiler;
import org.aurora.compiler.CompilationPipeline;
import org.aurora.interceptor.AurBytecodeDisassemblerInterceptor;
import org.aurora.interceptor.AurBytecodePrinterInterceptor;
import org.aurora.interceptor.AurParsedASTPrinterInterceptor;
import org.aurora.parser.AurParsePass;
import org.aurora.scanner.AurScanPass;
import org.aurora.util.AurFile;

public class Aurora {
    public static void main(String[] args) {
        CompilationPipeline compilationPipeline = new CompilationPipeline();

        compilationPipeline.insertStage(new AurScanPass());
        compilationPipeline.insertStage(new AurParsePass());
        compilationPipeline.insertStage(new AurCompiler());
        compilationPipeline.insertStage(new AurBytecodeEmissionPass()
                .addInterceptor(new AurBytecodeDisassemblerInterceptor()));

        AurFile file = new AurFile(args[0]);

        try {
            compilationPipeline.runWithInterceptors(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}