package org.aurora.core;

import org.aurora.compiler.CompilationPipeline;
import org.aurora.project.*;
import org.aurora.runtime.AurExecutionEnvironment;
import org.aurora.runtime.CompilationPipelineBuilder;
import org.aurora.util.AurFile;

public class Aurora {
    public static void main(String[] args) {
        CompilationPipelineBuilder pipelineBuilder = new CompilationPipelineBuilder();
        AurProjectFileReader projectFileReader = new AurProjectFileReader(args[0]);
        AurProject project = projectFileReader.getProject();
        AurFile file = new AurFile(project.main());
        CompilationPipeline compilationPipeline = pipelineBuilder.build(project);
        AurExecutionEnvironment executor = new AurExecutionEnvironment();
        executor.execute(compilationPipeline, project, file);
    }
}