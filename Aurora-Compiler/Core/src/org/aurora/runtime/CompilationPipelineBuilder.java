package org.aurora.runtime;

import org.aurora.binary.AurBytecodeEmissionPass;
import org.aurora.compiler.AurCSharpCompilerPass;
import org.aurora.compiler.AurCompilePass;
import org.aurora.compiler.AurPythonCompilerPass;
import org.aurora.compiler.CompilationPipeline;
import org.aurora.emulator.AurVirtualMachine;
import org.aurora.external.interceptor.AurInterceptorLoader;
import org.aurora.external.interceptor.AurInterceptorResolver;
import org.aurora.interceptor.AurPassiveInterceptor;
import org.aurora.interpreter.AurInterpretPass;
import org.aurora.optimizer.AurOptimizationPass;
import org.aurora.parser.AurParsePass;
import org.aurora.project.AurExternalInterceptorConfiguration;
import org.aurora.project.AurOptimizationLevel;
import org.aurora.project.AurProject;
import org.aurora.scanner.AurScanPass;

import java.util.List;

@SuppressWarnings("rawtypes")
public class CompilationPipelineBuilder {

    public CompilationPipeline build(AurProject project) {

        CompilationPipeline compilationPipeline = new CompilationPipeline();

        AurScanPass scanPass = new AurScanPass();
        AurParsePass parsePass = new AurParsePass();
        AurOptimizationPass optimizationPass = new AurOptimizationPass();
        //AurCompilePass compilePass = new AurCompilePass();
        //AurPythonCompilerPass compilePass = new AurPythonCompilerPass();
        AurCSharpCompilerPass compilePass = new AurCSharpCompilerPass();
        //AurBytecodeEmissionPass emissionPass = new AurBytecodeEmissionPass();
        //AurVirtualMachine virtualMachine = new AurVirtualMachine();
        AurInterpretPass interpretPass = new AurInterpretPass();

        compilationPipeline.insertStage(scanPass);
        compilationPipeline.insertStage(parsePass);

        if (project.environment().optimizationLevel() == AurOptimizationLevel.MAX) {
            compilationPipeline.insertStage(optimizationPass);
        }

        if (!project.isScript()) {
            compilationPipeline.insertStage(compilePass);
            //compilationPipeline.insertStage(emissionPass);
            //compilationPipeline.insertStage(virtualMachine);
        }

        if (project.isScript()) {
            compilationPipeline.insertStage(interpretPass);
        } else {
            AurExternalInterceptorConfiguration externalInterceptorDescriptor = project.externalInterceptorConfiguration();
            if (externalInterceptorDescriptor.enableExternalInterceptors()) {
                AurInterceptorLoader interceptorLoader = new AurInterceptorLoader();
                List<AurPassiveInterceptor> interceptors = interceptorLoader.loadExternalInterceptors();

                AurInterceptorResolver interceptorResolver = new AurInterceptorResolver();
                interceptorResolver.attachInterceptors(interceptors, compilationPipeline,
                        project);
            }
        }

        return compilationPipeline;
    }
}
