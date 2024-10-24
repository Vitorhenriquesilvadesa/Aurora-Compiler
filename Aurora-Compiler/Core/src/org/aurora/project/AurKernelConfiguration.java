package org.aurora.project;

public record AurKernelConfiguration(Boolean enabled, AurGPUTarget gpuTarget, Float memoryLimits) {
}
