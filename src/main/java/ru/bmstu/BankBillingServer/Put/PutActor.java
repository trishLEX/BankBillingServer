package ru.bmstu.BankBillingServer.Put;

import akka.actor.AbstractActor;
import akka.http.javadsl.model.StatusCodes;
import akka.japi.pf.ReceiveBuilder;
import org.hibernate.Session;
import ru.bmstu.BankBillingServer.AccountsEntity.AccountsEntity;
import ru.bmstu.BankBillingServer.HibernateSessionFactory;

public class PutActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(DepositMessage.class, msg ->
                        deposit(msg.getAccountID(), msg.getSum())
                )
                .match(WithdrawMessage.class, msg ->
                        withdraw(msg.getAccountID(), msg.getSum())
                )
                .build();
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
}
