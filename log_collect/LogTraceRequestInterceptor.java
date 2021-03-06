package nju.edu.cn.log.log_tracking.log_collect;

import nju.edu.cn.log.log_tracking.log_context.LogContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import feign.RequestInterceptor;
import feign.RequestTemplate;
@Configuration
public class LogTraceRequestInterceptor implements RequestInterceptor {

    @Autowired
    private LogContext logContext;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(LogContext.TRACE_ID_HEADER_KEY, logContext.getTraceId()+"");
        requestTemplate.header(LogContext.PARENT_SPAN_ID_HEADER_KEY,logContext.getSpanId()+"");
        requestTemplate.header(LogContext.SPAN_ID_HEADER_KEY,logContext.getNextSpanId()+"");
        requestTemplate.header(LogContext.IS_LOG_KEY,logContext.isLog()+"");
    }
}