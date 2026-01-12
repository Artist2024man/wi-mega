package com.wuin.wi_mega.common.resp;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Hidden
@ControllerAdvice
public class ResponseHandler implements ResponseBodyAdvice<Object> {

    private static final String[] swaggerPath = {"swagger-resources", "/v3/api-docs", "/doc.html", "favicon.ico", "/webjars/**"};

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (isSwagger(serverHttpRequest)
                || !mediaType.includes(MediaType.APPLICATION_JSON)
                || body instanceof RespModel) {
            if (body instanceof RespModel) {
                ((RespModel) body).setRequestId(MDC.get("traceId"));
            }
            return body;
        } else {
            RespModel respModel = RespModel.success(body);
            respModel.setRequestId(MDC.get("traceId"));
            return respModel;
        }
    }

    private Boolean isSwagger(ServerHttpRequest request) {
        for (String path : swaggerPath) {
            if (request.getURI().getPath().contains(path)) {
                return true;
            }
        }
        return false;
    }

}
