package com.jeremyliao.plugin


import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by liaohailiang on 2018/12/26.
 * demo for read property
 */
class PropertyReadPlugin implements Plugin<Project> {

    final String TAG = "[PropertyReadPlugin]"

    @Override
    void apply(Project project) {
        readSystemProperty()
        readProperty(project)
        readFile(project)
    }

    /**
     * 读取SystemProperty的方法
     * 定义在gradle.property中
     * 如：systemProp.demoPro=helloworld
     */
    private void readSystemProperty() {
        System.out.println(TAG + " system property demoPro: " + System.getProperty("demoPro"))
    }

    /**
     * 读取Property的方法
     * 定义在gradle.property中
     * 如：a.b.c=helloworld
     */
    private void readProperty(Project project) {
        def props = new Properties()
        props.load(project.rootProject.file('gradle.properties').newDataInputStream())
        def property = props.getProperty("a.b.c")
        System.out.println(TAG + " property a.b.c: " + property)
    }

    /**
     * 读取file的方法
     *
     */
    private void readFile(Project project) {
        File file = project.rootProject.file('white_list.prop')
        def lines = file.readLines()
        System.out.println(TAG + " readLines: " + lines)
    }
}
