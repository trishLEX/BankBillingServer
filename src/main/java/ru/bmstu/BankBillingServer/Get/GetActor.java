package ru.bmstu.BankBillingServer.Get;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import org.hibernate.Session;
import ru.bmstu.BankBillingServer.AccountsEntity.AccountsEntity;
import ru.bmstu.BankBillingServer.HibernateSessionFactory;

import java.util.HashMap;
import java.util.List;

public class GetActor extends AbstractActor {
    private static final String ACCOUNTS = "accounts";

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(GetBalanceMessage.class, msg ->
                        getBalance(msg.getAccountID())
                )
                .match(String.class, msg -> {
                            if (msg.equals(ACCOUNTS))
                                getAccounts();
                        }
                )
                .build();
    }

    private void getBalance(int id) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        AccountsEntity account = session.get(AccountsEntity.class, id);

        if (account == null) {
            sender().tell(-1.0, self());
        } else {
            sender().tell(account.getMoney(), self());
        }

        session.close();
    }

    private void getAccounts() {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();

        List<AccountsEntity> list = session.createCriteria(AccountsEntity.class).list();

        HashMap<Integer, Double> res = new HashMap<>();

        for (AccountsEntity account: list) {
            res.put(account.getId(), account.getMoney());
        }

        sender().tell(res, self());

        session.close();
    }
}
