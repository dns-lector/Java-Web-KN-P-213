package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.db.DbService;

@Singleton
public class AuthDao {
    private final DbService dbService;

    @Inject
    public AuthDao(DbService dbService) {
        this.dbService = dbService;
    }

    public void install() {
        String sql = "CREATE TABLE  IF NOT EXISTS  ";
    }
}
/*
DAO - Data Access Object
набір інструментів (бізнес-логіка) для роботи з
DTO - Data Transfer Object (Entities) - моделями
передачі даних

Задачі авторизації / автентифікації
[users]       [users_access]     [users_roles]     [tokens]
|user_id      |access_id         |role_id          |token_id
|user_name    |user_id           |role_name        |user_id
|email        |login             |can_create       |iat
|phone        |salt              |can_read         |exp
|avatar       |dk                |can_update       |
              |role_id           |can_delete       |

Д.З. Реалізувати сервіс для генерування випадкових імен файлів
(без розширення) - випадковий набір символів одного реєстру
у якому немає активних символів файлової системи (./*?\...)
Сервіс може приймати параметр - довжина імені (у символах),
якщо не передається, то вживає дані за замовчанням (з ентропією 64 біти)
Інжектувати до домашньої сторінки, вивести пробні результати
різної довжини

 */
