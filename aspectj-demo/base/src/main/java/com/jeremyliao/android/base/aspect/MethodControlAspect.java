package com.jeremyliao.android.base.aspect;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by liaohailiang on 2019-09-26.
 */
@Aspect
public class MethodControlAspect {

    private static String TAG = "[ExecutionTime]";

    @Pointcut("within(@com.jeremyliao.android.base.annotation.MethodControl *)")
    public void codeInTarget() {
    }

    @Pointcut("execution(public * *(..))")
    public void publicMethod() {
    }

    @Around("codeInTarget() && publicMethod()")
    public Object printExcutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimeMillis = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTimeMillis;
        Log.d(TAG, joinPoint.getSignature() + " execution cost time: " + executionTime + "ms");
        return result;
    }

}
