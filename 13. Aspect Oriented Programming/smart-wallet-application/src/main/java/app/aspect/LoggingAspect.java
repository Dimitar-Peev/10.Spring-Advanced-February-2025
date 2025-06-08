package app.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect // Клас
@Component
public class LoggingAspect {

//    @After("execution(* app.web.IndexController.*(..))")
//    public void logIndexControllerMethods() {
//
//        System.out.println("Hey, another method in IndexController was executed!");
//    }

    // Advice - метод с допълнителна логика
    @After("bean(userController)")  // Pointcut - "regex" for methods
    public void logIndexControllerMethods(JoinPoint joinPoint) {

        System.out.println("Hey, another method in UserController was executed!");
    }

//    @Before("@annotation(app.aspect.VeryImportant)")
//    public void logVeryImportantMethodExecution(JoinPoint joinPoint) {
//        System.out.println("Before method execution: " + joinPoint.getSignature().getName());
//        log.info("Before method execution: {}", joinPoint.getSignature().getName());
//    }

//    @AfterReturning(value = "@annotation(app.aspect.VeryImportant)", returning = "user")
//    public void logVeryImportantMethodExecution(JoinPoint joinPoint, User user) {
//        user.setUsername("USER_123");
//        System.out.println("After method execution: " + joinPoint.getSignature().getName());
//        log.info("After method execution: {}", joinPoint.getSignature().getName());
//        log.info("Returned user: {}", user);
//    }

//    @AfterThrowing(value = "@annotation(app.aspect.VeryImportant)", throwing = "exception")
//    public void logVeryImportantMethodExecution(JoinPoint joinPoint, Exception exception) {
//        System.out.println("Exception thrown in method: " + joinPoint.getSignature().getName());
//        log.error("Exception thrown in method: {}", joinPoint.getSignature().getName(), exception);
//
//        AuthenticationMetadata principal = (AuthenticationMetadata) SecurityContextHolder.getContext().getAuthentication();
//        log.info("User: {}", principal.getUsername());
//    }

    @Around(value = "@annotation(app.aspect.VeryImportant)")
    public Object logVeryImportantMethodExecution(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        System.out.println("Before method execution!");
        Object methodResult = proceedingJoinPoint.proceed();
        System.out.println("After method execution!");

        return methodResult;
    }
}
