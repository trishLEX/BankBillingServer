# BankBillingServer

Сервер размещается на localhost:9696.

## API:

* POST:
    * /bankaccount/{id}?id={id} - заводится новый счёт.
    * Параметры:
        * id - номер счёта.
    * Пример: http://localhost:9696/bankaccount/5?id=5
    * Ответы:
        * OK - успешное создание счёта;
        * BAD REQUEST - ошибка в запросе;
        * NOT ACCEPTABLE - ошибка выполнения.
        
* PUT:
    * /bankaccount/{id}/deposit?id={id}&sum={sum} - внесение суммы на счёт.
        * Параметры:
            * id - номер счёта;
            * sum - сумма для внесения.
        * Пример: http://localhost:9696/bankaccount/5/deposit?id=1&sum=5000.0
        * Ответы:
            * OK - успешное внесение суммы;
            * BAD REQUEST - ошибка в запросе;
            * NOT ACCEPTABLE - ошибка выполенения.
    * /bankaccount/{id}/withdraw?id={id}&sum={sum} - снятие суммы со счёта.
        * Параметры:
            * id - номер счёта;
            * sum - сумма для снятия.
        * Пример: http://localhost:9696/bankaccount/5/withdraw?id=1&sum=5000.0
        * Ответы:
            * OK - успешное снятие суммы;
            * BAD REQUEST - ошибка в запросе;
            * NOT ACCEPTABLE - ошибка выполенения.
            
* GET:
    * /bankaccount/{id}/balance?id={id} - получение баланса.
        * Параметры:
            * id - номер счёта.
        * Пример: http://localhost:9696/bankaccount/1/balance?id=5
        * Ответы:
            * 5000.0 - баланс счёта;
            * BAD REQUEST - ошибка в запросе;
            * NOT ACCEPTABLE - ошибка выполенения.
    * /bankaccount - получение таблицы - {"номер счёта": "баланс"}.
        * Пример: http://localhost:9696/bankaccount
        * Ответы:
            * {
                 "5": 5000
              }
            * BAD REQUEST - ошибка в запросе;
            * NOT ACCEPTABLE - ошибка выполенения. 
            
* DELETE:
    * /bankaccount/{id}/delete?id={id} - удаление счёта.
        * Параметры:
            * id - номер счёта.
         * Пример: http://localhost:9696/bankaccount/5/delete?id=5
         * Ответы:
             * OK - успешное удаление счёта;
             * BAD REQUEST - ошибка в запросе;
             * NOT ACCEPTABLE - ошибка выполенения.
        
## Установка:
В качестве СУБД используется PostgreSQL.
1. Создать базу данных BillingDB.
2. Создать в базе данных BillingDB схему billing_schema.
3. Создать в схеме billing_schema таблицу accounts:  
billingdb.billing_schema.accounts (  
  id INT PRIMARY KEY NOT NULL,  
  money FLOAT NOT NULL CHECK (money >= 0) DEFAULT 0  
);
4. Для работы из Intellij Idea необходимо добавить базу данных BillingDB в DataSources.
5. В файле resources/hibernate.cfg.xml в свойствах username и password прописать валидные имя пользователя и пароль к базе данных.
6. В файле resources/hibernate.cfg.xml в свойсте url прописать строку соединения для бызы данных.
7. Запуск производится из файла Server.java (там находится main, который поднимает сервер).
8. Тесты находятся в tests/ru/bmstu/BankBillingServer/ServerTest. Сделано интуитивное API для тестирования сервера. 

