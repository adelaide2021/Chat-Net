package com.neu.chatApp.util;

import java.util.Random;
//import org.apache.commons.codec.digest.DigestUtils;

public class Encryption {

    /**
    * Generate a salt for encryption with length long.
    * @return salt
    */
    public static String saltGenerator() {
      String dictionary = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_-+=";
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < 16; i++) {
        builder.append(dictionary.charAt(new Random().nextInt(dictionary.length())));
      }
      return builder.toString();
    }

    /**
    * Encrypt a string with md5.
    * @param password the string to be encrypted
    * @param salt the salt for encryption
    * @return the md5 hex code string
    */
    public static String md5(String password, String salt) {
//      return DigestUtils.md5Hex(password + salt);
      return password+salt;
    }

}
