package com.jeremyliao.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
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
    final String BUTTERKNIFE_GRADLE_PLUGIN_NAME = "com.jakewharton:butterknife-gradle-plugin:%s"
    final String PLUGIN1_NAME = "com.jeremyliao.gradle:plugin1:0.0.1"

    @Override
    void apply(Project project) {
        def props = new Properties()
        props.load(project.rootProject.file('gradle.properties').newDataInputStream())
        def butterknifeGradlePlugin = sprintf(BUTTERKNIFE_GRADLE_PLUGIN_NAME,
                props.getProperty("BUTTERKNIFE_PLUGIN_VERSION"))
        System.out.println(TAG + project)
        System.out.println(TAG + props.getProperty("BUTTERKNIFE_PLUGIN_VERSION"))
        System.out.println(TAG + System.getProperty("foo"))
        System.out.println(TAG + butterknifeGradlePlugin)

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
                project.buildscript.dependencies.classpath(PLUGIN1_NAME)
                project.buildscript.dependencies.classpath(butterknifeGradlePlugin)
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
