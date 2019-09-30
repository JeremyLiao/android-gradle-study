package com.jeremyliao.android.plugin;

import com.jeremyliao.android.base.annotation.PrintLog;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.Type;

/**
 * Created by liaohailiang on 2019-09-30.
 */
public class PrintLogClassVisitor extends ClassVisitor implements Opcodes {

    private String className;
    private boolean inject = false;

    public PrintLogClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean b) {
        if (Type.getDescriptor(PrintLog.class).equals(desc)) {
            inject = true;
        }
        return super.visitAnnotation(desc, b);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (!inject) {
            return mv;
        }
        return new AdviceAdapter(Opcodes.ASM5, mv, access, name, desc) {

            @Override
            public AnnotationVisitor visitAnnotation(String s, boolean b) {
                return super.visitAnnotation(s, b);
            }

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter();
                mv.visitLdcInsn("PrintLog");
                mv.visitLdcInsn("enter method");
                mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                mv.visitInsn(POP);
            }

            @Override
            protected void onMethodExit(int i) {
                super.onMethodExit(i);
                mv.visitLdcInsn("PrintLog");
                mv.visitLdcInsn("exit method");
                mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                mv.visitInsn(POP);
            }
        };
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
