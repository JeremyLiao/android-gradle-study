package com.jeremyliao.plugin;

import com.android.build.gradle.BaseExtension;
import com.jeremyliao.plugin.extension.DemoExtension;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.internal.initialization.DefaultScriptHandler;

import groovy.lang.Closure;

/**
 * Created by liaohailiang on 2018/12/26.
 */
public class AgsJavaPlugin implements Plugin<Project> {

    private static final String TAG = "[AgsJavaPlugin]";

    @Override
    public void apply(Project project) {
        System.out.println(TAG + project);
        BaseExtension baseExtension = project.getExtensions().findByType(BaseExtension.class);
        System.out.println(TAG + baseExtension);

        final DemoExtension extension = project.getExtensions().create("demoConfig", DemoExtension.class);
        System.out.println(TAG + "extension: " + extension);

        String testProperty = (String) project.property("testProperty");
        System.out.println(TAG + "testProperty: " + testProperty);

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                System.out.println(TAG + "extension afterEvaluate: " + extension);
            }
        });
    }
}
