package ru.bmstu.BankBillingServer.Post;

import akka.actor.AbstractActor;
import akka.http.javadsl.model.StatusCodes;
import akka.japi.pf.ReceiveBuilder;
import org.hibernate.Session;
import ru.bmstu.BankBillingServer.AccountsEntity.AccountsEntity;
import ru.bmstu.BankBillingServer.HibernateSessionFactory;

public class PostActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(CreateAccountMessage.class, msg ->
                        createAccount(msg.getAccountID())
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
}
