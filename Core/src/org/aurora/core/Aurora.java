package org.aurora.core;

import org.aurora.binary.AurBytecodeEmissionPass;
import org.aurora.compiler.AurCompilePass;
import org.aurora.compiler.CompilationPipeline;
import org.aurora.emulator.AurVirtualMachine;
import org.aurora.interceptor.AurBytecodeDecompilerInterceptor;
import org.aurora.interpreter.AurInterpretPass;
import org.aurora.parser.AurParsePass;
import org.aurora.scanner.AurScanPass;
import org.aurora.util.AurFile;

public class Aurora {
    public static void main(String[] args) {
        CompilationPipeline compilationPipeline = new CompilationPipeline();

        compilationPipeline.insertStage(new AurScanPass());
        compilationPipeline.insertStage(new AurParsePass());
        //compilationPipeline.insertStage(new AurCompilePass());
        //compilationPipeline.insertStage(new AurBytecodeEmissionPass().addInterceptor(new AurBytecodeDecompilerInterceptor()));
        //compilationPipeline.insertStage(new AurVirtualMachine());
        compilationPipeline.insertStage(new AurInterpretPass());

        AurFile file = new AurFile(args[0]);

        try {
            compilationPipeline.runWithInterceptors(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}