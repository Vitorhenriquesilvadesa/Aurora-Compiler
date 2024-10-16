package org.aurora.core;

import org.aurora.binary.AurBytecodeEmissionPass;
import org.aurora.compiler.AurCompilePass;
import org.aurora.compiler.CompilationPipeline;
import org.aurora.component.AurIOComponent;
import org.aurora.emulator.AurVirtualMachine;
import org.aurora.external.interceptor.AurInterceptorLoader;
import org.aurora.interceptor.AurPassiveInterceptor;
import org.aurora.interpreter.AurInterpretPass;
import org.aurora.optimizer.AurOptimizationPass;
import org.aurora.parser.AurParsePass;
import org.aurora.pass.AurCompilationPass;
import org.aurora.project.AurExternalInterceptorConfiguration;
import org.aurora.project.AurInterceptorConfiguration;
import org.aurora.project.AurOptimizationLevel;
import org.aurora.project.AurProjectFileReader;
import org.aurora.scanner.AurScanPass;
import org.aurora.util.AurFile;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Aurora {
    public static void main(String[] args) {
        CompilationPipeline compilationPipeline = new CompilationPipeline();

        AurScanPass scanPass = new AurScanPass();
        AurParsePass parsePass = new AurParsePass();
        AurOptimizationPass optimizationPass = new AurOptimizationPass();
        AurCompilePass compilePass = new AurCompilePass();
        AurBytecodeEmissionPass emissionPass = new AurBytecodeEmissionPass();
        AurVirtualMachine virtualMachine = new AurVirtualMachine();
        AurInterpretPass interpretPass = new AurInterpretPass();

        AurProjectFileReader projectFileReader = new AurProjectFileReader(args[0]);
        AurInterceptorConfiguration interceptorDescriptor = projectFileReader.getProject().interceptorConfiguration();
        AurExternalInterceptorConfiguration externalInterceptorDescriptor = projectFileReader.getProject().externalInterceptorConfiguration();

        AurFile file = new AurFile(projectFileReader.getProject().main());

        compilationPipeline.insertStage(scanPass);
        compilationPipeline.insertStage(parsePass);

        if (projectFileReader.getProject().environment().optimizationLevel() == AurOptimizationLevel.MAX) {
            compilationPipeline.insertStage(optimizationPass);
        }

        if (!projectFileReader.getProject().isScript()) {
            compilationPipeline.insertStage(compilePass);
            compilationPipeline.insertStage(emissionPass);
            compilationPipeline.insertStage(virtualMachine);
        }

        if (projectFileReader.getProject().isScript()) {
            compilationPipeline.insertStage(interpretPass);
        } else {

            if (externalInterceptorDescriptor.enableExternalInterceptors()) {
                List<AurPassiveInterceptor> interceptors = AurInterceptorLoader.loadExternalInterceptors();

                for (AurPassiveInterceptor interceptor : interceptors) {
                    Type[] genericInterfaces = interceptor.getClass().getGenericInterfaces();
                    for (Type type : genericInterfaces) {
                        if (type instanceof ParameterizedType parameterizedType) {
                            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                            if (actualTypeArguments.length == 2) {
                                Type inputType = actualTypeArguments[0];
                                Type outputType = actualTypeArguments[1];

                                for (AurCompilationPass<? extends AurIOComponent, ? extends AurIOComponent> pass : compilationPipeline.getPasses()) {
                                    if (inputType == pass.getInputType() && outputType == pass.getOutputType()
                                    && projectFileReader.getProject().externalInterceptorConfiguration().names().contains(interceptor.getName())) {
                                        //noinspection unchecked
                                        pass.addInterceptor(interceptor);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        try {
            if (projectFileReader.getProject().environment().debug()) {
                System.out.println("Compiling and running " + projectFileReader.getProject().descriptor().name() + " " +
                        projectFileReader.getProject().descriptor().version() + ".");
                compilationPipeline.runWithInterceptors(file);
            } else {
                System.out.println("Compiling and running " + projectFileReader.getProject().descriptor().name() + " " +
                        projectFileReader.getProject().descriptor().version() + ".");
                compilationPipeline.run(file);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}