package org.aurora.external.interceptor;

import org.aurora.compiler.CompilationPipeline;
import org.aurora.component.AurIOComponent;
import org.aurora.interceptor.AurPassiveInterceptor;
import org.aurora.pass.AurCompilationPass;
import org.aurora.project.AurProject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@SuppressWarnings("rawtypes")
public class AurInterceptorResolver {

    public void attachInterceptors(List<AurPassiveInterceptor> interceptors, CompilationPipeline compilationPipeline, AurProject project) {
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
                                    && project.externalInterceptorConfiguration().names().contains(interceptor.getName())) {
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
