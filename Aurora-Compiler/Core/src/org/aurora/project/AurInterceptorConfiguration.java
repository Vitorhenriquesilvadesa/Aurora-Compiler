package org.aurora.project;

import java.util.List;

public record AurInterceptorConfiguration(List<AurInterceptorDescriptor> interceptors) {

    public boolean isInterceptorActive(String name) {
        return interceptors.stream()
                .filter(interceptor -> interceptor.name().equals(name))
                .findFirst()
                .map(AurInterceptorDescriptor::isActive)
                .orElse(false);
    }
}
