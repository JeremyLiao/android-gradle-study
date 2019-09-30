package com.jeremyliao.plugin

import com.android.build.gradle.AppExtension
import com.jeremyliao.transform.AgsTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult

/**
 * Created by liaohailiang on 2018/12/26.
 */
class DependencyPlugin implements Plugin<Project> {

    final String TAG = "[DependencyPlugin]"

    final LinkedList<DependencyResult> dependencyStack = new LinkedList<>();

    @Override
    void apply(Project project) {
        project.task('group': 'demo', 'dependency') << {
            project.android.applicationVariants.all { variant ->
                System.out.println(TAG + "variant: " + variant.name)
                System.out.println("*******************Dependency Result Start*******************")
                project.configurations.each { Configuration configuration ->
                    if (configuration.name.toLowerCase().contains("${variant.name}runtimeclasspath")) {
                        configuration.incoming.resolutionResult.root.dependencies.each { DependencyResult dr ->
                            checkDependency(dr)
                        }
                    }
                }
                System.out.println("*******************Dependency Result End*******************")
            }
        }
    }

    private void checkDependency(DependencyResult dr) {
        if (dr instanceof UnresolvedDependencyResult) {
        } else if (dr instanceof ResolvedDependencyResult) {
            def rdr = (ResolvedDependencyResult) dr
            def size = rdr.selected.dependencies.size()
            if (size > 0) {
                dependencyStack.push(dr)
                rdr.selected.dependencies.each { DependencyResult dependencyResult ->
                    checkDependency(dependencyResult)
                }
                dependencyStack.pop()
            } else {
                printDependency(dr)
            }
        }
    }

    private void printDependency(DependencyResult dr) {
        StringBuilder stringBuilder = new StringBuilder()
        for (DependencyResult result : dependencyStack) {
            stringBuilder.append(result.requested.displayName).append("/")
        }
        stringBuilder.append(dr.requested.displayName)
        System.out.println(stringBuilder.toString())
    }
}
