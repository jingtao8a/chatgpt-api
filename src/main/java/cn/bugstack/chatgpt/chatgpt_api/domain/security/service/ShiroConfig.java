package cn.bugstack.chatgpt.chatgpt_api.domain.security.service;

import cn.bugstack.chatgpt.chatgpt_api.domain.security.service.realm.JwtRealm;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@Configuration
public class ShiroConfig {
    @Bean
    public SubjectFactory subjectFactory() {
        class JwtDefaultSubjectFacotry extends DefaultWebSubjectFactory {
            @Override
            public Subject createSubject(SubjectContext context) {
                context.setSessionCreationEnabled(false);
                return super.createSubject(context);
            }
        }
        return new JwtDefaultSubjectFacotry();
    }

    @Bean
    public Realm realm() {
        return new JwtRealm();
    }

    @Bean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm());
        //关闭ShiroDAO的功能
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        //不需要将Shiro Session中的东西存到任何地方（包括Http session中)
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        //禁止Subject的getSession方法
        securityManager.setSubjectFactory(subjectFactory());
        return securityManager;
    }
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager());
        //设置登入页面
        shiroFilter.setLoginUrl("/unauthenticated");
        //设置未授权页面
        shiroFilter.setUnauthorizedUrl("/unauthorized");
        //添加jwt过滤器
        Map<String, Filter> filterMap = new HashMap<>();
        //设置过滤器[anon \ logout可以不设置]
        filterMap.put("anon", new AnonymousFilter());
        filterMap.put("jwt", new JwtFilter());
        filterMap.put("logout", new LogoutFilter());
        shiroFilter.setFilters(filterMap);

        //拦截器，指定方法走哪个拦截器 [login->anon] [logout->logout] [verify->jwt]
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        filterRuleMap.put("/login", "anon");
        filterRuleMap.put("/logout", "logout");
        filterRuleMap.put("/verify", "jwt");
        shiroFilter.setFilterChainDefinitionMap(filterRuleMap);

        return shiroFilter;
    }
}
