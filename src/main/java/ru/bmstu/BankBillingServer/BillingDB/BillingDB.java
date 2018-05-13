package ru.bmstu.BankBillingServer.BillingDB;

import akka.actor.AbstractActor;
import akka.http.javadsl.model.StatusCodes;
import akka.japi.pf.ReceiveBuilder;
import org.hibernate.Session;
import ru.bmstu.BankBillingServer.AccountManagement.*;
import ru.bmstu.BankBillingServer.AccountsEntity.AccountsEntity;
import ru.bmstu.BankBillingServer.HibernateSessionFactory;

public class BillingDB extends AbstractActor {

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(CreateAccountMessage.class, msg ->
                        createAccount(msg.getAccountID())
                )
                .match(DepositMessage.class, msg ->
                        deposit(msg.getAccountID(), msg.getSum())
                )
                .match(WithdrawMessage.class, msg ->
                        withdraw(msg.getAccountID(), msg.getSum())
                )
                .match(GetBalanceMessage.class, msg ->
                        getBalance(msg.getAccountID())
                )
                .match(DeleteAccountMessage.class, msg ->
                        deleteAccount(msg.getAccountID())
                )
                .build();
    }

    private void createAccount(int id) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();

        if (session.get(AccountsEntity.class, id) == null) {

            AccountsEntity account = new AccountsEntity();
            account.setId(id);
            account.setMoney(0.0f);

            session.save(account);
            session.getTransaction().commit();

            sender().tell(StatusCodes.OK, self());
        } else {
            sender().tell(StatusCodes.BAD_REQUEST, self());
        }

        session.close();
    }

    private void deposit(int id, float sum) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        AccountsEntity account = session.get(AccountsEntity.class, id);

        if (account == null || sum < 0) {
            sender().tell(StatusCodes.BAD_REQUEST, self());
        } else {
            account.setMoney(account.getMoney() + sum);

            session.save(account);
            session.getTransaction().commit();

            sender().tell(StatusCodes.OK, self());
        }

        session.close();
    }

    private void withdraw(int id, float sum) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        AccountsEntity account = session.get(AccountsEntity.class, id);

        if (account == null || sum < 0) {
            sender().tell(StatusCodes.BAD_REQUEST, self());
        } else {
            double res = account.getMoney() - sum;

            if (res < 0) {
                sender().tell(StatusCodes.BAD_REQUEST, self());
            } else {
                account.setMoney(account.getMoney() - sum);

                session.save(account);
                session.getTransaction().commit();

                sender().tell(StatusCodes.OK, self());
            }
        }

        session.close();
    }

    private void getBalance(int id) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        AccountsEntity account = session.get(AccountsEntity.class, id);

        if (account == null) {
            sender().tell(null, self());
        } else {
            sender().tell(account.getMoney(), self());
        }

        session.close();
    }

    private void deleteAccount(int id) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        AccountsEntity account = session.get(AccountsEntity.class, id);

        if (account == null || account.getMoney() != 0.0) {
            sender().tell(StatusCodes.BAD_REQUEST, self());
        } else {
            session.delete(account);
            session.getTransaction().commit();

            sender().tell(StatusCodes.OK, self());
        }

        session.close();
    }
}
