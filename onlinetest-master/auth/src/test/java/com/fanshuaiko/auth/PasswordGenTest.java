package com.fanshuaiko.auth;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.junit.Test;

public class PasswordGenTest {

    @Test
    public void generatePasswords() {
        String algorithmName = "MD5";
        String password = "123456";
        int hashIterations = 2;

        String[] users = {"admin", "2026001", "1001"};

        System.out.println("--- Password Generation Start ---");
        for (String salt : users) {
            SimpleHash hash = new SimpleHash(algorithmName, password, salt, hashIterations);
            System.out.printf("User/Salt: %s, Hash: %s%n", salt, hash.toHex());
        }
        System.out.println("--- Password Generation End ---");
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter("passwords.txt", "UTF-8");
            for (String salt : users) {
                SimpleHash hash = new SimpleHash(algorithmName, password, salt, hashIterations);
                String line = String.format("User/Salt: %s, Hash: %s", salt, hash.toHex());
                System.out.println(line);
                writer.println(line);
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
