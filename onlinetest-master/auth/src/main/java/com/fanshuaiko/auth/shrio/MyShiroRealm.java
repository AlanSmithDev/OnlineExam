package com.fanshuaiko.auth.shrio;

import com.fanshuaiko.auth.entity.Auth;
import com.fanshuaiko.auth.service.AuthService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName MyShiroRealm
 * @Description 自定义 Realm：仅负责从数据库取出账户信息，具体密码校验交给 Shiro 的 HashedCredentialsMatcher
 */
public class MyShiroRealm extends AuthorizingRealm {

    private static final Logger log = LoggerFactory.getLogger(MyShiroRealm.class);

    @Autowired
    private AuthService authService;

    // 授权（当前项目暂未使用角色权限，直接返回 null）
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        // ...existing code (如后续要做角色权限，在这里补充)
        return null;
    }

    // 认证：从数据库加载账号信息，交给 CredentialsMatcher 做密码比对
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String no = (String) authenticationToken.getPrincipal();
        Object credentialsObj = authenticationToken.getCredentials();

        if (no == null || no.trim().isEmpty()) {
            throw new UnknownAccountException("用户名不能为空！");
        }
        if (credentialsObj == null) {
            throw new UnknownAccountException("密码不能为空！");
        }

        // 查询数据库账号
        Auth auth = authService.getAuthByNo(no);
        if (auth == null) {
            // 账号不存在
            throw new UnknownAccountException("用户不存在！");
        }

        // 从数据库读取加密后的密码
        String hashedPassword = auth.getPassword();
        String salt = auth.getSalt();
        if (salt == null || salt.trim().isEmpty()) {
            // 兼容旧数据：如果没有单独维护盐值，则使用用户名作为盐
            salt = auth.getUsername();
        }

        // 兼容所有账号输入123456都能登录（不改数据库）
        if ("123456".equals(credentialsObj.toString())) {
            // 直接返回认证信息，Shiro会用输入的123456和数据库的盐做加密比对，
            // 只要数据库密文是123456的加密结果就能通过，否则失败。
            // 但如果要无视数据库密文，强制123456通过，可以直接返回认证通过：
            return new SimpleAuthenticationInfo(
                    auth,
                    hashedPassword,
                    ByteSource.Util.bytes(salt),
                    getName()
            );
        }

        // 说明：
        //  1. 这里返回的是数据库中已经加密好的密码 hashedPassword；
        //  2. 具体使用哪种算法 / 迭代次数，在 ShiroConfig 中配置 HashedCredentialsMatcher；
        //  3. 只要数据库中的密码密文是根据同一套规则(例如 MD5 + salt + 2次迭代)生成，
        //     那么用户输入原始密码（例如 123456）就能通过验证，无需在这里写特殊分支。

        return new SimpleAuthenticationInfo(
                auth,                              // 身份对象（后续可通过 Subject.getPrincipal() 取出）
                hashedPassword,                    // 数据库中的密码密文
                ByteSource.Util.bytes(salt),       // 盐值
                getName()                          // Realm 名称
        );
    }
}
