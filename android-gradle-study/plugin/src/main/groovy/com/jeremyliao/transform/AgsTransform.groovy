package com.jeremyliao.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager

import java.util.function.Consumer
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Created by liaohailiang on 2018/12/26.
 */
class AgsTransform extends Transform {

    final String TAG = "[AgsTransform]"

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        System.out.println(TAG + "start transform")
        super.transform(transformInvocation)
        //处理输入
        System.out.println(TAG + "处理输入")
        for (TransformInput input : transformInvocation.inputs) {
            input.jarInputs.parallelStream().forEach(new Consumer<JarInput>() {
                @Override
                void accept(JarInput jarInput) {
                    File file = jarInput.getFile()
                    JarFile jarFile = new JarFile(file)
                    Enumeration<JarEntry> entries = jarFile.entries()
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement()
//                        System.out.println(TAG + "JarEntry: " + entry)
                    }
                }
            })
        }
        //处理输出
        System.out.println(TAG + "处理输出")
        File dest = transformInvocation.outputProvider.getContentLocation(
                "output_name",
                TransformManager.CONTENT_CLASS,
                TransformManager.PROJECT_ONLY,
                Format.DIRECTORY)
    }

    @Override
    String getName() {
        return AgsTransform.simpleName
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }
}
