package com.jeremyliao.android.plugin

import com.android.build.gradle.AppExtension
import com.jeremyliao.android.plugin.transform.PrintLogTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by liaohailiang on 2018/12/26.
 */
class PrintLogPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def app = project.extensions.findByType(AppExtension.class)
        app.registerTransform(new PrintLogTransform())
    }
}
