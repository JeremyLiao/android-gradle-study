package com.jeremyliao.plugin

import com.android.build.gradle.AppExtension
import com.jeremyliao.transform.AgsTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by liaohailiang on 2018/12/26.
 */
class AgsGroovyPlugin implements Plugin<Project> {

    final String TAG = "[AgsGroovyPlugin]"

    @Override
    void apply(Project project) {
        System.out.println(TAG + project)
        addNewTask(project)
        configAfterEvaluate(project)
        addTransform(project)
    }

    private void addNewTask(Project project) {
        //添加一个新的task
        project.task('group': 'demo', 'demo') << {
            System.out.println(TAG + "execute task demo")
        }
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
