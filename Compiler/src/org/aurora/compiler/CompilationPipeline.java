package org.aurora.compiler;

import org.aurora.component.AurIOComponent;
import org.aurora.pass.AurCompilationPass;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CompilationPipeline {

    private final List<AurCompilationPass<? extends AurIOComponent, ? extends AurIOComponent>> passes = new ArrayList<>();

    public void insertStage(AurCompilationPass<? extends AurIOComponent, ? extends AurIOComponent> pass) {
        passes.add(pass);
    }

    public void run(AurIOComponent input) {
        AurIOComponent currentInput = input;
        System.out.println("Running without interceptors.");
        for (AurCompilationPass<? extends AurIOComponent, ? extends AurIOComponent> pass : passes) {
            currentInput = runPass(pass, currentInput);
        }
    }

    public void runWithInterceptors(AurIOComponent input) {
        AurIOComponent currentInput = input;
        System.out.println("Running with interceptors.");

        for (AurCompilationPass<? extends AurIOComponent, ? extends AurIOComponent> pass : passes) {
            currentInput = runPassWithInterceptors(pass, currentInput);
        }
    }

    private <I extends AurIOComponent, O extends AurIOComponent> AurIOComponent runPass(AurCompilationPass<I, O> pass, AurIOComponent input) {
        if (!pass.getInputType().isInstance(input)) {
            throw new IllegalArgumentException("Input type mismatch. Expected: " + pass.getInputType() + ", but got: " + input.getClass());
        }
        return pass.run((I) input);
    }

    private <I extends AurIOComponent, O extends AurIOComponent> AurIOComponent runPassWithInterceptors(AurCompilationPass<I, O> pass, AurIOComponent input) {
        if (!pass.getInputType().isInstance(input)) {
            throw new IllegalArgumentException("Input type mismatch. Expected: " + pass.getInputType() + ", but got: " + input.getClass());
        }
        return pass.runWithInterceptors((I) input);
    }

    public List<AurCompilationPass<? extends AurIOComponent, ? extends AurIOComponent>> getPasses() {
        return passes;
    }
}
