package com.jeremyliao.plugin


import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by liaohailiang on 2018/12/26.
 * 动态添加classpath的例子
 */
class DynamicClasspathPlugin implements Plugin<Project> {

    final String TAG = "[DynamicClasspathPlugin]"
    final String BUTTERKNIFE_PLUGIN_NAME = "com.jakewharton:butterknife-gradle-plugin:%s"
    final String PLUGIN1_NAME = "com.jeremyliao.gradle:plugin1:%s"
    final String USER_REPOSITORIES = null

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
            //添加repo
            project.buildscript.repositories {
                //如果需要，添加自定义的repo
                if (USER_REPOSITORIES != null && USER_REPOSITORIES.length() > 0) {
                    project.buildscript.repositories.maven {
                        url USER_REPOSITORIES
                    }
                }
                //把root project中的repo添加进来
                project.buildscript.repositories.addAll(project.rootProject.buildscript.repositories)
            }
            //添加classpath
            project.buildscript.dependencies {
                //动态添加两个classpath
                def butterknifePlugin = sprintf(BUTTERKNIFE_PLUGIN_NAME, props.getProperty("BUTTERKNIFE_PLUGIN_VERSION"))
                def plugins1 = sprintf(PLUGIN1_NAME, props.getProperty("PLUGIN1_VERSION"))
                project.buildscript.dependencies.classpath(butterknifePlugin)
                project.buildscript.dependencies.classpath(plugins1)
                //添加完之后，还要把root project中的classpath添加进来
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
