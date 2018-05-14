package ru.bmstu.BankBillingServer.Delete;

import akka.actor.AbstractActor;
import akka.http.javadsl.model.StatusCodes;
import akka.japi.pf.ReceiveBuilder;
import org.hibernate.Session;
import ru.bmstu.BankBillingServer.AccountsEntity.AccountsEntity;
import ru.bmstu.BankBillingServer.HibernateSessionFactory;

public class DeleteActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(DeleteAccountMessage.class, msg ->
                        deleteAccount(msg.getAccountID())
                )
                .build();
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
