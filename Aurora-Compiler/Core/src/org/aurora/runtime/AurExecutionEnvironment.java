package org.aurora.runtime;

import org.aurora.compiler.CompilationPipeline;
import org.aurora.project.AurProject;
import org.aurora.util.AurFile;

public class AurExecutionEnvironment {
    public void execute(CompilationPipeline compilationPipeline, AurProject project, AurFile main) {
        try {
            if (project.environment().debug()) {
                System.out.println("Compiling and running " + project.descriptor().name() + " " +
                        project.descriptor().version() + ".");
                compilationPipeline.runWithInterceptors(main);
            } else {
                System.out.println("Compiling and running " + project.descriptor().name() + " " +
                        project.descriptor().version() + ".");
                compilationPipeline.run(main);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
