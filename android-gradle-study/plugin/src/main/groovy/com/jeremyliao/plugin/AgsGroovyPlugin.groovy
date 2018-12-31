package com.jeremyliao.plugin

import com.android.build.gradle.AppExtension
import com.jeremyliao.transform.AgsTransform
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.internal.artifacts.DefaultArtifactRepositoryContainer
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler
import org.gradle.api.internal.initialization.DefaultScriptHandler


/**
 * Created by liaohailiang on 2018/12/26.
 */
class AgsGroovyPlugin implements Plugin<Project> {

    final String TAG = "[AgsGroovyPlugin]"

    @Override
    void apply(Project project) {
        System.out.println(TAG + project)
        testSystemProperty(project)
        configAfterEvaluate(project)
        addTransform(project)
    }

    private void testSystemProperty(Project project) {
        System.out.println(TAG + System.getProperty("foo"))
    }

    private void configAfterEvaluate(Project project) {
        project.afterEvaluate {
            System.out.println(TAG + "execute afterEvaluate: " + project)
            def extension = project.extensions.findByType(AppExtension.class)
            extension.applicationVariants.all { variant ->
                String variantName = capitalize(variant.getName())
                Task mergeJavaResTask = project.tasks.findByName(
                        "transformResourcesWithMergeJavaResFor" + variantName)
                System.out.println(TAG + "mergeJavaResTask: " + mergeJavaResTask)
                mergeJavaResTask.doLast {
                    System.out.println(TAG + "mergeJavaResTask.doLast execute")
                }
            }
        }
    }

    private void addTransform(Project project) {
        def extension = project.extensions.findByType(AppExtension.class)
        System.out.println(TAG + extension)
        extension.registerTransform(new AgsTransform())
    }

    private String capitalize(CharSequence str) {
        return (str == null || str.length() == 0) ? "" : "" + Character.toUpperCase(str.charAt(0)) + str.subSequence(1, str.length())
    }
}
