package org.aurora.core;

import org.aurora.compiler.CompilationPipeline;
import org.aurora.interceptor.AurScanTokenPrinterInterceptor;
import org.aurora.parser.AurParsePass;
import org.aurora.scanner.AurScanPass;
import org.aurora.util.AurFile;

public class Aurora {
    public static void main(String[] args) {
        CompilationPipeline compilationPipeline = new CompilationPipeline();

        compilationPipeline.insertStage(new AurScanPass().addInterceptor(new AurScanTokenPrinterInterceptor()));
        compilationPipeline.insertStage(new AurParsePass());

        AurFile file = new AurFile(args[0]);

        try {
            compilationPipeline.runWithInterceptors(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}