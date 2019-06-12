package lab_7.message.registration;
import java.io.Serializable;

/**
 * Запрос пользователя на регистрацию.
 */
public final class RegistrationRequest implements Serializable {
    public String login;
    public String email;
}
