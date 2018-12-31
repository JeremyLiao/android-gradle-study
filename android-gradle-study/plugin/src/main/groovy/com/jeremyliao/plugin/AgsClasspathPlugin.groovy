package com.jeremyliao.plugin

import com.jeremyliao.transform.AgsTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler

/**
 * Created by liaohailiang on 2018/12/26.
 */
class AgsClasspathPlugin implements Plugin<Project> {

    final String TAG = "[AgsClasspathPlugin]"
    final String BUTTERKNIFE_PLUGIN_NAME = "com.jakewharton:butterknife-gradle-plugin:%s"
    final String PLUGIN1_NAME = "com.jeremyliao.gradle:plugin1:%s"

    @Override
    void apply(Project project) {
        System.out.println(TAG + project)
        addDependencies(project)
    }

    private void addDependencies(Project project) {
        def props = new Properties()
        props.load(project.rootProject.file('gradle.properties').newDataInputStream())

        def rootClassPath = project.rootProject.buildscript.getScriptClassPath()
        System.out.println(TAG + "rootClassPath: " + rootClassPath)

        project.buildscript {
            project.buildscript.repositories {
                def rootRepositories = project.rootProject.buildscript.repositories
                System.out.println(TAG + "rootRepositories: " + rootRepositories)
                DefaultRepositoryHandler handler = project.buildscript.repositories
                handler.addAll(rootRepositories)
            }
            project.buildscript.dependencies {
                def butterknifePlugin = sprintf(BUTTERKNIFE_PLUGIN_NAME,
                        props.getProperty("BUTTERKNIFE_PLUGIN_VERSION"))
                def plugins1 = sprintf(PLUGIN1_NAME,
                        props.getProperty("PLUGIN1_VERSION"))
                System.out.println(TAG + "butterknifePlugin: " + butterknifePlugin)
                System.out.println(TAG + "plugins1: " + plugins1)
                project.buildscript.dependencies.classpath(butterknifePlugin)
                project.buildscript.dependencies.classpath(plugins1)
                getRootDependencies(project).all {
                    dependency ->
                        def classpath = sprintf("%s:%s:%s", dependency.group, dependency.name, dependency.version)
                        System.out.println(TAG + "add classpath: " + classpath)
                        project.buildscript.dependencies.classpath(classpath)
                }
            }
        }
    }

    private def getRootDependencies(Project project) {
        def clazz = Class.forName("org.gradle.api.internal.initialization.DefaultScriptHandler")
        def field = clazz.getDeclaredField("classpathConfiguration")
        field.setAccessible(true)
        def classpathConfiguration = field.get(project.rootProject.buildscript)
        def rootDependencies = classpathConfiguration.getAllDependencies()
        System.out.println(TAG + "rootDependencies: " + rootDependencies)
        return rootDependencies
    }
}
