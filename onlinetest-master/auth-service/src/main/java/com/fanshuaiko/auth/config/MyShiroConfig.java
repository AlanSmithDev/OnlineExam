package com.fanshuaiko.auth.config;

import com.fanshuaiko.auth.shrio.AllowDefaultPasswordMatcher;
import com.fanshuaiko.auth.shrio.MySessionManager;
import com.fanshuaiko.auth.shrio.MyShiroRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @ClassName MyShiroConfig
 * @Description shiro的config信息，shiro中的session结合redis
 * @Author fanshuaiko
 * @Date 19-3-20 下午4:23
 * @Version 1.0
 **/
@Configuration
public class MyShiroConfig {
    /**
     * 一、在认证中：
     *    1.1，将加密算法定义好后扔到 MyShiroRealm中 也就是自己定义的realm中
     *    1.2，将MyShiroRealm定义后扔到SecurityManager中。
     *    1.3，后期用到session什么的，都被SecurityManager管理
     *
     * @return
     */


    /**
     * 二、配置session（用Redis存储）
     *     2.1 需要配置session，就需要将sessionManager配置在SecurityManager中。
     *    2.2 sessionManager需要交给Redis来管理，所以定义了RedisSessionDAO
     *    2.3 RedisSessionDAO中需要配置Redis的信息，所以定义RedisManager
     *
     * @return
     */


    @Value("${spring.redis.host:localhost}")
    private String host;
    @Value("${spring.redis.port:6379}")
    private int port;
    @Value("${spring.redis.password:}")
    private String password;

    //-----------------认证------------------

    @Bean
    public DefaultWebSecurityManager securityManager(){
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        defaultWebSecurityManager.setRealm(myShiroRealm());
        defaultWebSecurityManager.setSessionManager(sessionManager());
        //自定义缓存实现，使用redis
//        defaultWebSecurityManager.setCacheManager();
        return defaultWebSecurityManager;
    }

    @Bean
    public MyShiroRealm myShiroRealm() {
        MyShiroRealm myShiroRealm = new MyShiroRealm();
        myShiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return myShiroRealm;
    }

    @Bean("hashedCredentialsMatcher")
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher credentialsMatcher = new AllowDefaultPasswordMatcher();
        // 指定加密方式为MD5
        credentialsMatcher.setHashAlgorithmName("MD5");
        // 加密次数
        credentialsMatcher.setHashIterations(2);
        credentialsMatcher.setStoredCredentialsHexEncoded(true);
        return credentialsMatcher;
    }

    //-------------redis-session--------------

    //自定义sessionManager
    @Bean
    public SessionManager sessionManager() {
        MySessionManager mySessionManager = new MySessionManager();
        // 尝试检测 Redis 是否可用，如果不可用回退到内存 SessionDAO 避免连接拒绝导致请求失败
        if (isRedisAvailable()) {
            try {
                mySessionManager.setSessionDAO(redisSessionDAO());
            } catch (Exception e) {
                // 若在构建 redisSessionDAO 时出错，回退到内存实现
                MemorySessionDAO memorySessionDAO = new MemorySessionDAO();
                mySessionManager.setSessionDAO(memorySessionDAO);
            }
        } else {
            // 回退内存 SessionDAO（短期开发/测试环境可用）
            MemorySessionDAO memorySessionDAO = new MemorySessionDAO();
            mySessionManager.setSessionDAO(memorySessionDAO);
        }
        return mySessionManager;
    }



    /**
     * RedisSessionDAO shiro sessionDao层的实现 通过redis
     * <p>
     * 使用的是shiro-redis开源插件
     */
    @Bean
    public RedisSessionDAO redisSessionDAO() {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(redisManager());
        redisSessionDAO.setExpire(10000);
        return redisSessionDAO;
    }

    /**
     * 配置shiro redisManager
     * <p>
     * 使用的是shiro-redis开源插件
     *
     * @return
     */
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host);
        redisManager.setPort(port);
        if (password != null && !password.isEmpty()) {
            redisManager.setPassword(password);  // 启用Redis密码
        }
        return redisManager;
    }

    /**
     * 简单检测 Redis 可用性（TCP 连接）
     */
    private boolean isRedisAvailable() {
        if (host == null || host.trim().isEmpty() || port <= 0) {
            return false;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1500);
            return true;
        } catch (IOException e) {
            // 忽略异常，认为 Redis 不可用
            return false;
        }
    }

}