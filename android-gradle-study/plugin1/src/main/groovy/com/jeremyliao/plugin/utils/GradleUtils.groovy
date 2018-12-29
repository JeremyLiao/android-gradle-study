package com.jeremyliao.plugin.utils

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by liaohailiang on 2018/12/26.
 */
class GradleUtils {

    static String getGradlePluginVersion() {
        String version = null
        try {
            def clazz = Class.forName("com.android.builder.Version")
            def field = clazz.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION")
            field.setAccessible(true)
            version = field.get(null)
        } catch (Exception ignore) {
        }
        if (version == null) {
            try {
                def clazz = Class.forName("com.android.builder.model.Version")
                def field = clazz.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION")
                field.setAccessible(true)
                version = field.get(null)
            } catch (Exception ignore) {
            }
        }
        return version
    }

    static boolean isGradlePlugin300orAbove() {
        try {
            String gradlePluginVersion = getGradlePluginVersion()
            return gradlePluginVersion.compareTo("3.0.0") >= 0
        } catch (Throwable throwable) {

        }
        return false
    }
}
