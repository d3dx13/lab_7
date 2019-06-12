package lab_7.server;
import lab_7.message.Account;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Главный класс для работы с коллекциями аккаунтов и "Танцоров".
 * Позволяет получать информацию о коллекции,
 * А также безопасно её сохранять/загружать.
 * Также позволяет обновлять данные учётных записей.
 */
public class Database {
    /**
     * Информация о учётных записях пользователей.
     */
    public static ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<String, Account>();

    /**
     * Сохранить данные учётных записей.
     * Операция потокобезопасна.
     * @return Успешность операции.
     */
    public synchronized static boolean accountsSave() {
        try {
            accounts.values().parallelStream().forEach(account -> DatabaseSQL.saveAccount(account));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
    /**
     * Загрузить данные учётных записей.
     * Операция потокобезопасна.
     * @return Успешность операции.
     */
    public synchronized static boolean accountsLoad(){
        try {
                accountsSave();
            accounts = DatabaseSQL.loadAccounts();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
