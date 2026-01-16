package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Aspect
public class AutoFillAspect {
    @Before("@annotation(com.sky.annotation.AutoFill)")
    public void autoFill(JoinPoint joinPoint) throws NoSuchFieldException, IllegalAccessException {
        Object arg = joinPoint.getArgs()[0];
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        AutoFill autoFil = method.getAnnotation(AutoFill.class);
        OperationType operationType = autoFil.value();
        if (operationType == OperationType.UPDATE) {
            Class clazz = arg.getClass();
            Field updateTimeField = clazz.getDeclaredField("updateTime");
            Field updateUserField = clazz.getDeclaredField("updateUser");
            updateTimeField.setAccessible(true);
            updateUserField.setAccessible(true);
            updateTimeField.set(arg, LocalDateTime.now());
            updateUserField.set(arg, BaseContext.getCurrentId());
        }else if (operationType == OperationType.INSERT) {
            Class clazz = arg.getClass();
            Field createTimeField = clazz.getDeclaredField("createTime");
            Field createUserField = clazz.getDeclaredField("createUser");
            Field updateTimeField = clazz.getDeclaredField("updateTime");
            Field updateUserField = clazz.getDeclaredField("updateUser");
            createTimeField.setAccessible(true);
            createUserField.setAccessible(true);
            updateTimeField.setAccessible(true);
            updateUserField.setAccessible(true);
            createTimeField.set(arg, LocalDateTime.now());
            createUserField.set(arg, BaseContext.getCurrentId());
            updateTimeField.set(arg, LocalDateTime.now());
            updateUserField.set(arg, BaseContext.getCurrentId());
        }
    }
}
