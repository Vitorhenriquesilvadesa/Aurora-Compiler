package org.aurora.compiler;

import org.aurora.component.AurIOComponent;
import org.aurora.pass.AurCompilationPass;

import java.util.ArrayList;
import java.util.List;

public class CompilationPipeline {

    private final List<AurCompilationPass<? extends AurIOComponent, ? extends AurIOComponent>> passes = new ArrayList<>();

    public CompilationPipeline insertStage(AurCompilationPass<? extends AurIOComponent, ? extends AurIOComponent> pass) {
        passes.add(pass);
        return this;
    }

    public void run(AurIOComponent input) throws Exception {
        AurIOComponent currentInput = input;
        for (AurCompilationPass<? extends AurIOComponent, ? extends AurIOComponent> pass : passes) {
            System.out.println("Running " + pass.getDebugName() + ".");
            currentInput = runPass(pass, currentInput);
        }
    }

    public void runWithInterceptors(AurIOComponent input) throws Exception {
        AurIOComponent currentInput = input;
        for (AurCompilationPass<? extends AurIOComponent, ? extends AurIOComponent> pass : passes) {
            System.out.println("Running " + pass.getDebugName() + " with interceptors.");
            currentInput = runPassWithInterceptors(pass, currentInput);
        }
    }

    @SuppressWarnings("unchecked")
    private <I, O> AurIOComponent runPass(AurCompilationPass<I, O> pass, AurIOComponent input) throws Exception {
        if (!pass.getInputType().isInstance(input)) {
            throw new IllegalArgumentException("Input type mismatch. Expected: " + pass.getInputType() + ", but got: " + input.getClass());
        }
        return (AurIOComponent) pass.run((I) input);
    }

    @SuppressWarnings("unchecked")
    private <I, O> AurIOComponent runPassWithInterceptors(AurCompilationPass<I, O> pass, AurIOComponent input) throws Exception {
        if (!pass.getInputType().isInstance(input)) {
            throw new IllegalArgumentException("Input type mismatch. Expected: " + pass.getInputType() + ", but got: " + input.getClass());
        }
        return (AurIOComponent) pass.runWithInterceptors((I) input);
    }
}
