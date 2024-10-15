package cn.bugstack.chatgpt.chatgpt_api.domain.security.service;

import cn.bugstack.chatgpt.chatgpt_api.domain.security.model.vo.JwtToken;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.slf4j.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFilter extends AccessControlFilter {
    private Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    /**
     * isAccessAllowed用于判断是否携带有效的JwtToken
     * 这里直接返回false， 让它走onAccessDenied流程
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;
    }

    /**
     * 返回true表示登入通过
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        //如果设定的token放到header 中，则可以这样获取：request.getHeader("Authorization")
        //如果设定的token是通过参数传递的，则可以像下面这样获取：request.getParameter("token")
        JwtToken jwtToken = new JwtToken(request.getParameter("token"));
        try {
            //鉴权认证
//            logger.info("onAccessDenied");
            getSubject(servletRequest, servletResponse).login(jwtToken);
            return true;
        } catch (Exception e) {
            logger.error("鉴权认证失败", e);
            onLoginFail(servletResponse);
            return false;
        }
    }

    /**
     * 鉴权认证失败时默认返回 401 状态码
     */
    private void onLoginFail(ServletResponse response) throws IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.getWriter().write("Auth Err!");
    }
}
