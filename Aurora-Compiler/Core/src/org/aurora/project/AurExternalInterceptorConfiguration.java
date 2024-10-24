package org.aurora.project;

import java.util.List;

public record AurExternalInterceptorConfiguration(boolean enableExternalInterceptors, List<String> names) {
}
