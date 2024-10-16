package org.aurora.project;

public record AurProject(AurProjectDescriptor descriptor, String main, Boolean isScript,
                         AurKernelConfiguration kernelConfiguration, AurDependencies dependencies,
                         AurEnvironmentDescriptor environment, AurInterceptorConfiguration interceptorConfiguration,
                         AurExternalInterceptorConfiguration externalInterceptorConfiguration) {
}
