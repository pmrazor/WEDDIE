package com.ssafy.exhi.base.aspect;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // Controller 패키지의 모든 메서드
    @Pointcut("execution(* com.ssafy.exhi.domain..controller..*(..))")

    public void controllerMethods() {
    }

    // Service 패키지의 모든 메서드
    @Pointcut("execution(* com.ssafy.exhi.domain..service..*(..))")
    public void serviceMethods() {
    }

    @Around("controllerMethods() || serviceMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!log.isInfoEnabled()) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("➡️ Method Start: {} with args: {}", methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        log.info("⬅️ Method End: {} return: {} ({} ms)", methodName, result, endTime - startTime);
        return result;
    }
}
