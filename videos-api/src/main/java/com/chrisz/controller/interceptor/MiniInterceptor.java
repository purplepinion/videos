package com.chrisz.controller.interceptor;

import com.chrisz.utils.JSONResult;
import com.chrisz.utils.JsonUtils;
import com.chrisz.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class MiniInterceptor implements HandlerInterceptor {

    @Autowired
    public RedisOperator redis;

    public static final String USER_REDIS_SESSION = "user_redis_session";

    /*拦截器在controller之前调用*/
    //true 放行
    //false 拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) throws Exception {
        String userId = request.getHeader("userId");
        String userToken = request.getHeader("userToken");

        //用户id和token都不为空
        if(StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)){

            String uniqueToken = redis.get(USER_REDIS_SESSION + ":" + userId);
            //token 过期
            if(StringUtils.isEmpty(uniqueToken) || StringUtils.isBlank(uniqueToken)){
                System.out.println("请登录1...");
                returnErrorResponse(response,new JSONResult().errorMsg("请登录..."));
                return false;
            }else{
                if(!uniqueToken.equals(userToken)){
                    System.out.println("账号被挤出...");
                    returnErrorResponse(response,new JSONResult().errorMsg("账号被挤出..."));
                    return false;
                }
            }
        }else{
            System.out.println("请登录2...");
            returnErrorResponse(response,new JSONResult().errorMsg("请登录..."));
            return false;
        }

        return true;
    }

    /*拦截器返回错误信息*/
    public void returnErrorResponse(HttpServletResponse response, JSONResult result)
            throws IOException, UnsupportedEncodingException {
        OutputStream out = null;
        try{
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/json");

            out = response.getOutputStream();
            out.write(JsonUtils.objectToJson(result).getBytes("utf-8"));
            out.flush();
        }finally {
            if(out!=null){
                out.close();
            }
        }

    }

    /*请求Controller后，渲染视图之前*/
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    /*渲染视图之后*/
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
