package org.aurora.interpreter;

import org.aurora.component.AurIOComponent;

public class AurInterpretResult extends AurIOComponent {

    private final int result;

    public AurInterpretResult(int result) {
        this.result = result;
    }

    @Override
    public AurIOComponent clone() {
        return new AurInterpretResult(result);
    }
}
