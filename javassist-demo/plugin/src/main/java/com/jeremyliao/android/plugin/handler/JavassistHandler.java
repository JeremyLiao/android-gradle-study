package com.jeremyliao.android.plugin.handler;

import com.android.build.api.transform.DirectoryInput;

import java.io.File;

import javassist.ClassPool;
import javassist.NotFoundException;

/**
 * Created by liaohailiang on 2019-10-15.
 */
public class JavassistHandler {

    //初始化类池
    private final static ClassPool pool = ClassPool.getDefault();

    public static void handle(DirectoryInput directoryInput) throws NotFoundException {
        File file = directoryInput.getFile();
        String absolutePath = file.getAbsolutePath();
        pool.appendClassPath(absolutePath);

        if (file.isDirectory()){

        }
    }
}
