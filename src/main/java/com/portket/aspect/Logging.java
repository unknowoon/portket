package com.portket.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class Logging {

    private static final String REQUEST_ID = "requestId";
    private static final String STARTED_AT = "startedAt";
    private static final String CALL_COUNT = "callCount";

    private static final String CONTROLLER = "within(com.portket.app.controller..*)";
    private static final String SERVICE = "within(com.portket.app.service..*)";
    private static final String CONTROLLER_OR_SERVICE = "within(com.portket.app.controller..*) || within(com.portket.app.service..*)";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Pointcut(CONTROLLER)
    private void pointcut(){}

    @Before("pointcut()")
    public void beforeController(JoinPoint joinPoint) {
        String startedAt = dateFormat.format(new Date());

        String requestId = UUID.randomUUID().toString().split("-")[0];
        String callCount = String.valueOf(1);

        MDC.put(REQUEST_ID, requestId);
        MDC.put(STARTED_AT, startedAt);
        MDC.put(CALL_COUNT, callCount);

        log.info("API Request ID: {} Started at {}", requestId, startedAt);

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.info("|{}| Controller: {}.{} called", requestId, className, methodName);
    }

    @Before(SERVICE)
    public void beforeService(JoinPoint joinPoint) {
        String requestId = MDC.get(REQUEST_ID);
        String callCnt = MDC.get(CALL_COUNT);
        int callCount = callCnt == null ? 0 : Integer.parseInt(callCnt);

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        Object[] args = joinPoint.getArgs();

        String padding = "    ".repeat(callCount);

        log.info("|{}| {}|- Service: {}.{} called", requestId, padding, className, methodName);

        if (args != null && args.length > 0) {
            log.info("|{}| {}    |- Parameters: {}", requestId, padding, Arrays.toString(args));
        } else {
            log.info("|{}| {}    |- No parameters", requestId, padding);
        }

        MDC.put(CALL_COUNT, String.valueOf(callCount + 1));

    }

    @AfterReturning(pointcut = SERVICE, returning = "result")
    public void afterReturningService(Object result) {
        String requestId = MDC.get(REQUEST_ID);
        String callCnt = MDC.get(CALL_COUNT);

        int callCount = callCnt == null ? 0 : Integer.parseInt(callCnt)-1;
        String padding = "    ".repeat(callCount);

        if (result == null) {
            log.info("|{}| {}    |- Return void", requestId, padding);
        } else {
            log.info("|{}| {}    |- Return Value: {}", requestId, padding, result);
        }

        MDC.put(CALL_COUNT, String.valueOf(callCount));
    }

    @AfterThrowing(pointcut  =CONTROLLER_OR_SERVICE, throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Throwable exception) {
        String requestId = MDC.get(REQUEST_ID);
        String timestamp = dateFormat.format(new Date());
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String exceptionType = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();
        String stackTrace = Arrays.toString(exception.getStackTrace());

        log.error("|{}| {}.{} encountered an ERROR!!! at {}", requestId, className, methodName, timestamp);
        log.error("|{}| Exception: {}, {}", requestId, exceptionType, exceptionMessage);
        log.error("|{}| Stack Trace: {}", requestId, stackTrace);
    }

    @AfterReturning(pointcut = CONTROLLER, returning = "result")
    public void afterReturning(Object result) {
        String timestamp = dateFormat.format(new Date());

        String requestId = MDC.get(REQUEST_ID);
        String startedAt = MDC.get(STARTED_AT);

        log.info("|{}| Response: {}", requestId, result);
        try {
            Date start = dateFormat.parse(startedAt);
            Date now = dateFormat.parse(timestamp);

            log.info("|{}| Execution Time: {} ms", requestId, now.getTime() - start.getTime());
        } catch (Exception e) {
            log.error("|{}| Failed to calculate execution time", requestId, e);
        } finally {
            MDC.clear();
        }
    }
}
