package org.aurora.project;

import org.aurora.exception.AurException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("ALL")
public class AurProjectFileReader {

    private AurProject project;

    public AurProjectFileReader(String projectFilepath) {
        String file = getFileContent(projectFilepath);
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(options);
        Map map = (Map) yaml.load(file);

        if (map.containsKey("project")) {
            project = loadProject((Map) map.get("project"));
        } else {
            throw new AurException("Could not load project");
        }
    }

    private AurProject loadProject(Map map) {
        AurProjectDescriptor descriptor = loadProjectDescriptor(map);
        String mainFile = map.get("main").toString();
        Boolean isScript = map.get("script").toString().equals("true");
        AurKernelConfiguration kernelConfiguration = loadKernelConfiguration(map);
        AurDependencies dependencies = loadDependencies(map);
        AurEnvironmentDescriptor environmentDescriptor = loadEnvironmentDescriptor(map);
        AurInterceptorConfiguration interceptorConfiguration = loadInterceptorConfiguration(map);
        AurExternalInterceptorConfiguration externalInterceptorConfiguration = loadExternalInterceptorConfiguration(map);
        return new AurProject(descriptor, mainFile, isScript, kernelConfiguration, dependencies, environmentDescriptor,
                interceptorConfiguration, externalInterceptorConfiguration);
    }

    private AurExternalInterceptorConfiguration loadExternalInterceptorConfiguration(Map map) {
        boolean enableExternalInterceptors = false;
        List<String> names = new ArrayList<>();

        if (map.containsKey("environment")) {
            Map environmentMap = (Map) map.get("environment");
            if (environmentMap.containsKey("enable-external-interceptors")) {
                enableExternalInterceptors = Boolean.parseBoolean(environmentMap.get("enable-external-interceptors").toString());
            }

            if (environmentMap.containsKey("external-interceptors")) {
                List<String> list = (List<String>) environmentMap.get("external-interceptors");
                names.addAll(list);
            }
        } else {
            throw new AurException("Could not load interceptors.");
        }

        return new

                AurExternalInterceptorConfiguration(enableExternalInterceptors, names);
    }

    private AurInterceptorConfiguration loadInterceptorConfiguration(Map map) {
        List<AurInterceptorDescriptor> interceptorDescriptors = new ArrayList<>();

        if (map.containsKey("environment")) {
            Map environmentMap = (Map) map.get("environment");

            if (environmentMap.containsKey("internal-interceptors")) {
                Map interceptorMap = (Map) environmentMap.get("internal-interceptors");

                for (Object key : interceptorMap.keySet()) {
                    interceptorDescriptors.add(new AurInterceptorDescriptor(key.toString(), interceptorMap.get(key).toString().equals("true")));
                }
            } else {
                // TODO
            }
        } else {
            throw new AurException("Could not load interceptors.");
        }

        return new AurInterceptorConfiguration(interceptorDescriptors);
    }

    private AurEnvironmentDescriptor loadEnvironmentDescriptor(Map map) {
        if (map.containsKey("environment")) {
            Map environmentMap = (Map) map.get("environment");
            return new AurEnvironmentDescriptor(environmentMap.get("debug").toString().equals("true"),
                    AurOptimizationLevel.valueOf(environmentMap.get("optimization-level").toString().toUpperCase()));
        } else {
            throw new AurException("No environment found.");
        }
    }

    private AurDependencies loadDependencies(Map map) {

        List<AurDependency> dependencies = new ArrayList<>();
        if (map.containsKey("dependencies")) {
            List<Map> dependenciesList = (List<Map>) map.get("dependencies");

            for (Map dependency : (List<Map>) dependenciesList) {
                dependencies.add(loadDependency(dependency));
            }
        }

        return new AurDependencies(dependencies);
    }

    private AurDependency loadDependency(Map dependency) {
        return new AurDependency(dependency.get("name").toString(), dependency.get("version").toString());
    }

    private AurKernelConfiguration loadKernelConfiguration(Map<String, Object> map) {
        if (map.containsKey("kernels")) {
            Map description = (Map) map.get("kernels");

            return new AurKernelConfiguration(description.get("enabled").toString().equals("true"),
                    AurGPUTarget.valueOf(description.get("gpu-target").toString().toUpperCase()),
                    parseMemoryLimits(description.get("memory-limits").toString()));
        } else {
            throw new AurException("Missing project description.");
        }
    }

    private Float parseMemoryLimits(String limits) {
        if (limits.contains("GB")) {
            String rawValue = limits.replaceAll("GB", "");
            return Float.parseFloat(rawValue) * 1024f * 1024f * 1024f;
        } else if (limits.contains("MB")) {
            String rawValue = limits.replaceAll("MB", "");
            return Float.parseFloat(rawValue) * 1024f * 1024f;
        } else if (limits.contains("KB")) {
            String rawValue = limits.replaceAll("KB", "");
            return Float.parseFloat(rawValue) * 1024f;
        } else {
            return Float.parseFloat(limits);
        }
    }

    private AurProjectDescriptor loadProjectDescriptor(Map map) {
        if (map.containsKey("description")) {
            Map description = (Map) map.get("description");
            return new AurProjectDescriptor((String) description.get("name"),
                    (String) description.get("version"),
                    (Boolean) description.get("alpha"));
        } else {
            throw new AurException("Missing project description.");
        }
    }

    private String getFileContent(String filepath) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

        } catch (IOException e) {
            throw new AurException(e.getMessage());
        }

        return sb.toString();
    }

    public AurProject getProject() {
        return project;
    }
}
