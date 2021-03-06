package com.baidu.disconf.client.store.aspect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.baidu.disconf.client.common.annotations.DisconfItem;
import com.baidu.disconf.client.store.DisconfStoreProcessor;
import com.baidu.disconf.client.store.DisconfStoreProcessorFactory;
import com.baidu.disconf.client.utils.MethodUtils;
import com.baidu.disconf.core.common.constants.DisConfigTypeEnum;

/**
 * 配置拦截
 * 
 * @author liaoqiqi
 * @version 2014-6-11
 */
@Service
@Aspect
public class DisconfAspectJ {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DisconfAspectJ.class);

    @Pointcut(value = "execution(public * *(..))")
    public void anyPublicMethod() {
    }

    /**
     * 获取配置文件数据
     * 
     * @param pjp
     * @param disconfFileItem
     * @return
     * @throws Throwable
     */
    @Around("anyPublicMethod() && @annotation(disconfFileItem)")
    public Object decideAccess(ProceedingJoinPoint pjp, DisconfFileItem disconfFileItem) throws Throwable {

        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();

        //
        // 文件名
        //
        Class<?> cls = method.getDeclaringClass();
        DisconfFile disconfFile = cls.getAnnotation(DisconfFile.class);

        //
        // Field名
        //
        Field field = MethodUtils.getFieldFromMethod(method, cls.getDeclaredFields(), DisConfigTypeEnum.FILE);
        if (field != null) {

            //
            // 请求仓库配置数据
            //
            DisconfStoreProcessor disconfStoreProcessor = DisconfStoreProcessorFactory.getDisconfStoreFileProcessor();
            Object ret = disconfStoreProcessor.getConfig(disconfFile.filename(), disconfFileItem.name());
            if (ret != null) {
                LOGGER.info("using disconf store value: " + disconfFile.filename() + " (" + disconfFileItem.name()
                        + " , " + ret + ")");
                return ret;
            }
        }

        Object rtnOb = null;

        try {
            // 返回原值
            rtnOb = pjp.proceed();
        } catch (Throwable t) {
            LOGGER.info(t.getMessage());
            throw t;
        }

        return rtnOb;
    }

    /**
     * 获取配置项数据
     * 
     * @param pjp
     * @param disconfFileItem
     * @return
     * @throws Throwable
     */
    @Around("anyPublicMethod() && @annotation(disconfItem)")
    public Object decideAccess(ProceedingJoinPoint pjp, DisconfItem disconfItem) throws Throwable {

        //
        // 请求仓库配置数据
        //
        DisconfStoreProcessor disconfStoreProcessor = DisconfStoreProcessorFactory.getDisconfStoreItemProcessor();
        Object ret = disconfStoreProcessor.getConfig(null, disconfItem.key());
        if (ret != null) {
            LOGGER.info("using disconf store value: (" + disconfItem.key() + " , " + ret + ")");
            return ret;
        }

        Object rtnOb = null;

        try {
            // 返回原值
            rtnOb = pjp.proceed();
        } catch (Throwable t) {
            LOGGER.info(t.getMessage());
            throw t;
        }

        return rtnOb;
    }
}
