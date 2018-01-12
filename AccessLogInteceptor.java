package nju.edu.cn.log.log_tracking;

import com.alibaba.fastjson.JSONObject;
import javafx.geometry.Pos;
import org.bouncycastle.cert.ocsp.Req;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by cong on 2018-01-03.
 * 拦截每一次http请求,如果没有traceId，需要生成一个traceId并且记录访问日志
 */
@Component("accessLogInteceptor")
public class AccessLogInteceptor implements HandlerInterceptor{

    @Resource
    private LogContext logContext;

    @Value("${spring.application.name}")
    private String sysName;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        LogContextBuilder builder=new LogContextBuilder(sysName);
        if(StringUtils.isEmpty(request.getHeader("traceId"))){
            builder.buildWithoutHeader(request,logContext);
            HandlerMethod handlerMethod=(HandlerMethod)o;

            AccessLogVO accessLogVO=new AccessLogVO();
            accessLogVO.setType(AccessTypeEnum.HTTP_REQUEST.getCode());
            accessLogVO.setTraceId(logContext.getTraceId());
            accessLogVO.setSpanId(logContext.getSpanId());
            accessLogVO.setParentSpanId(logContext.getParentSpanId());
            accessLogVO.setServiceUrl(request.getRequestURL().toString());
            String controller=handlerMethod.getBeanType().getSimpleName();
            accessLogVO.setServiceName(buildServiceName(controller,getServicePath(handlerMethod)));
            accessLogVO.setContent(readBody(request));
            accessLogVO.setTarget(logContext.getSysName());

            System.out.println(JSONObject.toJSONString(accessLogVO));
        }else{
            builder.buildWithHeader(request,logContext);
        }



        return true;
    }

    /**
     * 读取http中的值
     * @param request
     * @return
     */
    private String readBody(HttpServletRequest request) {
        BufferedReader reader = null;
        StringBuilder builder=new StringBuilder();
        try {
            reader= request.getReader();

            String str = "";
            while ((str = reader.readLine()) != null) {
                builder.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    private String buildServiceName(String controller,String path){
        ServiceNameBuilder builder=new ServiceNameBuilder();

        builder.append(logContext.getSysName());
        builder.append(controller);
        builder.append(path.split("/"));

        return builder.toString();
    }

    /**
     * 获取注解@RequestMapping配置的url
     * @param handlerMethod
     * @return
     */
    private String getServicePath(HandlerMethod handlerMethod){
        Method method=handlerMethod.getMethod();

        RequestMapping requestMapping=method.getAnnotation(RequestMapping.class);
        if(requestMapping!=null)
            return requestMapping.value()[0];

        PostMapping postMapping=method.getAnnotation(PostMapping.class);
        if(postMapping!=null)
            return postMapping.value()[0];

        GetMapping getMapping=method.getAnnotation(GetMapping.class);
        if(getMapping!=null)
            return getMapping.value()[0];

        return "";
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }


}