package com.museum.reservation.config;

import com.museum.reservation.repository.RequestLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLogInterceptor.class);

    private final RequestLogRepository requestLogRepository;

    public RequestLogInterceptor(RequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return;
        }
        int status = response.getStatus();
        boolean success = status < 400;
        String requestType = request.getMethod() + " " + request.getRequestURI();
        String message = success ? null : "请求失败(HTTP " + status + ")";
        try {
            requestLogRepository.insert(requestType, success, message);
        } catch (Exception exception) {
            log.warn("记录请求日志失败: {}", exception.getMessage());
        }
    }
}
