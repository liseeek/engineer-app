package com.example.medhub.aspect;

import com.example.medhub.aspect.annotation.Auditable;
import com.example.medhub.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            String actor = "anonymous";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                actor = auth.getName();
            }

            String resourceId = resolveResourceId(joinPoint, auditable.resourceId());

            String requestId = null;

            auditLogService.log(actor, auditable.action(), resourceId, requestId, "Auto-logged via Aspect");

        } catch (Exception e) {
            log.error("Failed to record audit log for {}", auditable.action(), e);
        }
    }

    private String resolveResourceId(JoinPoint joinPoint, String spelExpression) {
        if (spelExpression == null || spelExpression.isEmpty()) {
            return "N/A";
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), method, args, parameterNameDiscoverer);

        try {
            Expression expression = parser.parseExpression(spelExpression);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : "null";
        } catch (Exception e) {
            log.warn("Invalid SpEL expression for Audit: {}", spelExpression);
            return "ERROR";
        }
    }
}
