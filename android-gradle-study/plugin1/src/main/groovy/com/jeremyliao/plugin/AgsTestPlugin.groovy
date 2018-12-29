package com.jeremyliao.plugin

import com.jeremyliao.plugin.utils.GradleUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by liaohailiang on 2018/12/26.
 */
class AgsTestPlugin implements Plugin<Project> {

    final String TAG = "[AgsTestPlugin]"

    @Override
    void apply(Project project) {
        System.out.println(TAG + project)
        project.android.applicationVariants.all { variant ->
            System.out.println(TAG + "variant: " + variant)
            def packageTask = project.tasks.findByName("package${variant.name.capitalize()}")
            System.out.println(TAG + "packageTask: " + packageTask)
            System.out.println(TAG + "gradlePluginVersion: " + GradleUtils.gradlePluginVersion)
            System.out.println(TAG + "gradlePlugin300orAbove: " + GradleUtils.gradlePlugin300orAbove)
            packageTask.doFirst {
                System.out.println(TAG + "doFirst --> gradlePluginVersion: " + GradleUtils.gradlePluginVersion)
                System.out.println(TAG + "doFirst --> gradlePlugin300orAbove: " + GradleUtils.gradlePlugin300orAbove)
            }
        }
    }
}
