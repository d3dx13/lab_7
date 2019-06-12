package lab_7.server.core;

import lab_7.crypto.Mail;
import lab_7.message.Account;
import lab_7.message.loggingIn.AuthenticationRequest;
import lab_7.message.loggingIn.AuthenticationResponse;
import lab_7.message.loggingIn.IdentificationRequest;
import lab_7.message.loggingIn.IdentificationResponse;
import lab_7.message.registration.RegistrationRequest;
import lab_7.message.registration.RegistrationResponse;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import static lab_7.Settings.*;
import static lab_7.server.Database.accounts;
import static lab_7.server.Database.accountsSave;

/**
 * Класс для обработки запросов пользователя на регистрацию, идентификацию и аутентификацию.
 */
class SignUpInHandler {
    /**
     * Метод обработки запроса на регистрацию от пользователя.
     * @param request Запрос регистрации.
     * @return Ответ на запрос регистрации.
     */
    static RegistrationResponse registration(RegistrationRequest request){
        RegistrationResponse response = new RegistrationResponse();
        if (request.login.length() < loginMinimalLength){
            response.confirm = false;
            response.message = "login is short";
            return response;
        }
        if (request.login.length() > loginMaximalLength){
            response.confirm = false;
            response.message = "login is long";
            return response;
        }
        if (accounts.containsKey(request.login)){
            response.confirm = false;
            response.message = "user exists";
            return response;
        }
        Account tempAccount = new Account();
        tempAccount.login = request.login;
        String password;
        password = randomAlphaNumeric(8);
        StringBuilder head = new StringBuilder()
                .append(request.login)
                .append(" : ")
                .append(request.email)
                .append(" - ");
        try {
            System.out.println(head.append("Generating RSA pair; "));
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            RSAKeyGenParameterSpec kpgSpec = new RSAKeyGenParameterSpec(userRSAKeyLength, BigInteger.probablePrime(userRSAKeyLength - 1, new SecureRandom()));
            System.out.println(head.append("Generating RSA done; "));
            System.out.println(head.append("Generating encrypted AES passwords; "));
            keyPairGenerator.initialize(kpgSpec);
            KeyPair keyPair = keyPairGenerator.genKeyPair();
            tempAccount.publicKey = keyPair.getPublic().getEncoded();
            MessageDigest sha = MessageDigest.getInstance("SHA-224");
            byte[] secretKey = Arrays.copyOf(sha.digest(password.getBytes(Charset.forName("UTF-8"))), userAESKeySize);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            tempAccount.privateKey = cipher.doFinal(keyPair.getPrivate().getEncoded());
            System.out.println(head.append("Generating AES done; "));
        } catch (Exception ex){
            System.out.println(head.append("Generating error: ").append(ex.getMessage()).append("; "));
            response.confirm = false;
            response.message = "critical security error";
            return response;
        }
        try {
            Mail.sendMessage(request.email, "Ваш логин: \"" + tempAccount.login + "\"\nВаш пароль: \"" + password + "\"\nНе удаляйте это сообщение или перепишите пароль.\nКопии этого пароля не существует\n");
            System.out.println(head.append("Password has sent; "));
        } catch (Exception ex){
            System.out.println(head.append("Mail sending error: ").append(ex.getMessage()).append("; "));
            response.confirm = false;
            response.message = "Mail error: " + ex.getMessage();
            return response;
        }
        tempAccount.registrationDate = (new Date()).toString();
        if (accounts.containsKey(request.login)){
            response.confirm = false;
            response.message = "user exists";
            return response;
        }
        accounts.putIfAbsent(request.login, tempAccount);
        accountsSave();
        response.confirm = true;
        response.message = "success";
        return response;
    }
    /**
     * Метод обработки запроса на идентификацию от пользователя.
     * @param request Запрос идентификации.
     * @return Ответ на запрос идентификации.
     */
    static IdentificationResponse identification(IdentificationRequest request) {
        IdentificationResponse response = new IdentificationResponse();
        StringBuilder head = new StringBuilder()
                .append(request.login);
        System.out.println(head.append(" - trying to identify; "));
        try {
            if (request.login.length() < loginMinimalLength) {
                response.message = "login is short";
                System.out.println(head.append(response.message).append("; "));
                return response;
            }
            if (request.login.length() > loginMaximalLength) {
                response.message = "login is long";
                System.out.println(head.append(response.message).append("; "));
                return response;
            }
            if (!accounts.containsKey(request.login)) {
                response.message = "login is wrong";
                System.out.println(head.append(response.message).append("; "));
                return response;
            }
            Account user = accounts.get(request.login);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(user.publicKey)));
            SecureRandom secureRandom = new SecureRandom();
            user.random = new byte[identificationRandomSize];
            secureRandom.nextBytes(user.random);
            response.random = cipher.doFinal(user.random);
            response.privateKey = user.privateKey.clone();
            accounts.put(request.login, user);
            response.message = "success";
            System.out.println(head.append(response.message).append("; "));
            return response;
        } catch (Exception ex){
            response.message = ex.getMessage();
            System.out.println(head.append(response.message).append("; "));
            return response;
        }
    }
    /**
     * Метод обработки запроса на аутентификацию от пользователя.
     * @param request Запрос аутентификации.
     * @return Ответ на запрос аутентификации.
     */
    static AuthenticationResponse authentication(AuthenticationRequest request){
        AuthenticationResponse response = new AuthenticationResponse();
        StringBuilder head = new StringBuilder()
                .append(request.login);
        System.out.println(head.append(" - trying to authenticate; "));
        if (request.login.length() < loginMinimalLength) {
            response.message = "login is short";
            System.out.println(head.append(response.message).append("; "));
            return response;
        }
        if (request.login.length() > loginMaximalLength) {
            response.message = "login is long";
            System.out.println(head.append(response.message).append("; "));
            return response;
        }
        if (!accounts.containsKey(request.login)) {
            response.message = "login is wrong";
            System.out.println(head.append(response.message).append("; "));
            return response;
        }
        if (!(Arrays.equals(accounts.get(request.login).random, request.random))) {
            response.message = "random is wrong";
            System.out.println(head.append(response.message).append("; "));
            return response;
        } else {
            try {
                Account user = accounts.get(request.login);
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(user.publicKey)));
                SecureRandom secureRandom = new SecureRandom();
                user.secretKey = new byte[userAESKeySize];
                secureRandom.nextBytes(user.secretKey);
                response.secretKey = cipher.doFinal(user.secretKey);
                accounts.put(request.login, user);
                response.message = "success";
                System.out.println(head.append(response.message).append("; "));
                return response;
            } catch (Exception ex){
                response.message = ex.getMessage();
                System.out.println(head.append(response.message).append("; "));
                return response;
            }
        }
    }

    /**
     * Функция генерации случайной строки.
     * @param count длина
     * @return Ответ сервера
     */
    public static String randomAlphaNumeric(int count) {
        final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
