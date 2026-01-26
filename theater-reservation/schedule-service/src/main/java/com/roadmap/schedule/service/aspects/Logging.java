package com.roadmap.schedule.service.aspects;

import com.roadmap.schedule.service.exceptions.OverlappingException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.NoSuchElementException;

@Aspect
@Component
public class Logging {

    private Logger LOG = LoggerFactory.getLogger(Logging.class);

    // Pointcut: Matches any method in a class annotated with @RestControllerAdvice
    @Pointcut("within(@org.springframework.web.bind.annotation.RestControllerAdvice *)")
    public void advicePackage() {}

    // Pointcut: Matches any method annotated with @ExceptionHandler
    @Pointcut("@annotation(org.springframework.web.bind.annotation.ExceptionHandler)")
    public void exceptionHandlerMethod() {}

    // Advice: Run this logic BEFORE the exception handler method executes
    @Before("advicePackage() && exceptionHandlerMethod() && args(exception, ..)")
    public void logException(JoinPoint joinPoint, Exception exception) {

        // We can still retrieve the Request object if needed via RequestContextHolder
        // but often just logging the exception type and message is the goal here.
        HttpServletRequest request = getCurrentHttpRequest();
        LOG.info(request.getMethod());
        LOG.info(request.getRequestURI());
        LOG.info(request.getQueryString());
        LOG.info(request.getHeader("User-Agent"));
        String methodName = joinPoint.getSignature().getName();

        if (exception instanceof OverlappingException) {
            // Maybe just warn for runtime business exceptions
            LOG.warn("Handling overlapping exception in method: {} | Error: {} | Time: {}", methodName, exception.getMessage(), Instant.now());
        } else if (exception instanceof NoSuchElementException){
            // Log full stack trace for others
            LOG.error("Handling not found exception in method: {} | Error: {} | Time: {}", methodName, exception.getMessage(), Instant.now());
        } else {
            LOG.error("Handling unforeseen exception! " + Instant.now());
        }
    }
    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return (attrs != null) ? attrs.getRequest() : null;
        } catch (Exception e) {
            // Context might be null if this is running in a background thread or test
            return null;
        }
    }
}
