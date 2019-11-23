package com.jeremyliao.plugin


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

/**
 * Created by liaohailiang on 2018/12/26.
 */
class DependencyPlugin implements Plugin<Project> {

    final String TAG = "[DependencyPlugin]"
    final List<String> whiteList = new ArrayList<>()

    @Override
    void apply(Project project) {
        project.android.applicationVariants.all { variant ->
            println(TAG + "variant: " + variant.name)
            whiteList.clear()
            whiteList.addAll(project.rootProject.file('white_list.prop').readLines())
            project.configurations.each { Configuration configuration ->
                if (configuration.name.toLowerCase().contains("${variant.name}runtimeclasspath")) {
                    configuration.incoming.resolutionResult.root.dependencies.each { DependencyResult dr ->
                        boolean accept = acceptDependency(new Dependency(dr, null))
                        println(TAG + dr.requested.displayName + " : " + accept)
                    }
                }
            }
        }
    }

    private boolean acceptDependency(Dependency dependency) {
        def dr = dependency.dependencyResult
        if (accept(dr)) {
            return true
        }
        if (dr instanceof ResolvedDependencyResult) {
            def rdr = (ResolvedDependencyResult) dr
            def dependencies = rdr.selected.dependencies
            if (dependencies.size() > 0) {
                for (def dep : dependencies) {
                    def accept = acceptDependency(new Dependency(dep, dependency))
                    if (!accept) {
                        return false
                    }
                }
                return true
            }
        }
        println(TAG + "Not accept: " + getDependencyPath(dependency))
        return false
    }

    private boolean accept(DependencyResult dr) {
        def name = dr.requested.displayName
        for (String dependency : whiteList) {
            if (name == dependency) {
                return true
            }
        }
        return false
    }

    private String getDependencyPath(Dependency dependency) {
        List<String> strings = new ArrayList<>()
        Dependency current = dependency
        while (current != null) {
            strings.add(0, current.dependencyResult.requested.displayName)
            current = current.previousDependency
        }
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < strings.size(); i++) {
            sb.append(strings.get(i))
            if (i < strings.size() - 1) {
                sb.append(" --> ")
            }
        }
        return sb.toString();
    }

    static class Dependency {
        final DependencyResult dependencyResult
        final Dependency previousDependency

        Dependency(DependencyResult dependencyResult, Dependency previousDependency) {
            this.dependencyResult = dependencyResult
            this.previousDependency = previousDependency
        }
    }
}
