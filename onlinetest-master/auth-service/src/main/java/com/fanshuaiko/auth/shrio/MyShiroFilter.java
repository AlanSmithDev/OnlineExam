package com.fanshuaiko.auth.shrio;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName MyShiroFilter
 * @Description
 * @Author fanshuaiko
 * @Date 19-3-20 下午5:46
 * @Version 1.0
 **/
@Configuration
public class MyShiroFilter {
    private static final Logger log = LoggerFactory.getLogger(MyShiroFilter.class);

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        Map<String,Filter> map = new LinkedHashMap<String,Filter>();
        map.put("authc",getFormAuthenticationFilter());
        shiroFilterFactoryBean.setFilters(map);

        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        // 允许匿名访问登录接口
        filterChainDefinitionMap.put("/auth/**", "anon");
        // 放行文件上传接口
        filterChainDefinitionMap.put("/backage/profile/uploadAvatar", "anon");
        // 允许匿名访问swagger-ui
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/v2/api-docs", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        // 其他所有请求都需要认证
        filterChainDefinitionMap.put("/**", "authc");

        //登录的URL接口（Shiro可以进行识别）
        shiroFilterFactoryBean.setLoginUrl("/auth/login");
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }

    /**
     * 开启shiro aop注解支持.
     * 使用代理方式;所以需要开启代码支持;
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    MyFormAuthenticationFilter getFormAuthenticationFilter(){
        return new MyFormAuthenticationFilter();
    }
}
