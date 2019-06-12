package lab_7.client.core;

import lab_7.message.Crypted;
import lab_7.message.Message;
import lab_7.message.loggingIn.*;
import lab_7.message.registration.*;
import lab_7.crypto.ObjectCryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;

import static lab_7.Settings.*;

/**
 * Класс для реализации сетевой коммуникации на стороне клиента.
 */
public class NetworkConnection {
    /**
     * Отправить команду на сервер и получить ответ на неё.
     * @param message Команда
     * @return Ответ
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws ClassNotFoundException
     */
    public static Message command(Message message) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, ClassNotFoundException {
        Object response = objectSend(objectCryption.messageEncrypt(message));
        return objectCryption.messageDecrypt((Crypted)response);
    }
    /**
     * Настроить сетевое соединение: Установить Адрес сервера и порт.
     * @param hostname Адрес сервера
     * @param port Порт
     */
    public static void setServerAddress(String hostname, int port){
        serverAddress = new InetSocketAddress(hostname, port);
    }
    /**
     * @return Текущее соединение.
     */
    public static InetSocketAddress getServerAddress(){
        return serverAddress;
    }
    /**
     * Процесс регистрации.
     * @return Успешность
     */
    public static boolean signUp() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            RegistrationRequest registrationRequest = new RegistrationRequest();
            registrationRequest.login = objectCryption.getUserLogin();
            if (objectCryption.getUserLogin().length() < loginMinimalLength || objectCryption.getUserLogin().length() > loginMaximalLength) {
                System.out.println("!!! Login must be %d to %d characters !!!");
                return false;
            }

            System.out.println(new StringBuilder()
                    .append("Введите вашу электронную почту (на неё будет отправлен пароль): "));
            registrationRequest.email = reader.readLine().trim();
            System.out.println("Waiting for registration from the server");
            RegistrationResponse registrationResponse = registration(registrationRequest);
            System.out.print("Registration: ");
            if (registrationResponse.confirm) {
                System.out.println(registrationResponse.message);
                return true;
            } else
                System.out.println("failed\nReason: " + registrationResponse.message);
            return false;
        }catch (UnresolvedAddressException ex){
            System.out.println("Address is incorrect");
            return false;
        } catch (Exception ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }
    /**
     * Процесс авторизации.
     * @return Успешность
     */
    public static boolean signIn() {
        try {
            String password;
            Console console = System.console();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            IdentificationRequest identificationRequest = new IdentificationRequest();
            identificationRequest.login = NetworkConnection.objectCryption.getUserLogin();
            IdentificationResponse identificationResponse = identification(identificationRequest);
            AuthenticationRequest authenticationRequest = new AuthenticationRequest();
            System.out.print("Logging in...\nEnter your password: ");
            password = reader.readLine();
            //password = new String(console.readPassword());
            MessageDigest sha = MessageDigest.getInstance("SHA-224");
            SecretKeySpec secretKeySpec = new SecretKeySpec(Arrays.copyOf(sha.digest(password.getBytes(Charset.forName("UTF-8"))), userAESKeySize), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] privateKey;
            System.out.println(identificationResponse.message);
            try {
                privateKey = cipher.doFinal(identificationResponse.privateKey);
            } catch (Exception ex) {
                System.out.println("\nPassword incorrect");
                return false;
            }
            Cipher cipher2 = Cipher.getInstance("RSA");
            cipher2.init(Cipher.DECRYPT_MODE, KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKey)));
            try {
                authenticationRequest.random = cipher2.doFinal(identificationResponse.random);
            } catch (Exception e) {
                System.out.println("\nPassword incorrect");
                return false;
            }
            authenticationRequest.login = NetworkConnection.objectCryption.getUserLogin();
            AuthenticationResponse authenticationResponse = authentication(authenticationRequest);
            if (authenticationResponse.message.equals("success")) {
                byte[] secretKey;
                try {
                    secretKey = cipher2.doFinal(authenticationResponse.secretKey);
                } catch (Exception e) {
                    System.out.println("\nPassword incorrect");
                    return false;
                }
                objectCryption.setSecretKey(secretKey);
                return true;
            }
            System.out.println("Authentication failed: " + authenticationResponse.message);
            return false;
        } catch (Exception ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }
    /**
     * Отправить Object на сервер и получить Object в ответ.
     * @param message отправляемый Object
     * @return получаемый Object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static Object objectSend(Object message) throws IOException, ClassNotFoundException {
        SocketChannel server = SocketChannel.open(serverAddress);
        ByteBuffer outBuffer = ByteBuffer.wrap(objectCryption.messageSerialize(message));
        server.write(outBuffer);
        outBuffer.clear();
        ByteBuffer byteBuffer = ByteBuffer.allocate(clientReceiveBuffer);
        long time = Instant.now().getEpochSecond();
        while (server.read(byteBuffer) != -1 && (Instant.now().getEpochSecond() - time < clientReceiveTimeout)){ }
        Object response = objectCryption.messageDeserialize(byteBuffer.array());
        server.close();
        return response;
    }
    /**
     * Процесс идентификации пользователя.
     * @return Ответ сервера
     */
    private static IdentificationResponse identification (IdentificationRequest request) throws IOException, ClassNotFoundException {
        Object response = objectSend(request);
        return (IdentificationResponse)response;
    }
    /**
     * Процесс регистрации пользователя.
     * @return Ответ сервера
     */
    private static RegistrationResponse registration (RegistrationRequest request) throws IOException, ClassNotFoundException {
        Object response = objectSend(request);
        return (RegistrationResponse)response;
    }
    /**
     * Процесс аутентификации пользователя.
     * @return Ответ сервера
     */
    private static AuthenticationResponse authentication (AuthenticationRequest request) throws IOException, ClassNotFoundException {
        Object response = objectSend(request);
        return (AuthenticationResponse)response;
    }
    /**
     * Текущее сетевое соединение.
     */
    private static InetSocketAddress serverAddress;
    /**
     * Экземпляр класса ObjectCryption для работы с шифрованием и сериализацией.
     */
    public static ObjectCryption objectCryption = new ObjectCryption();
}
