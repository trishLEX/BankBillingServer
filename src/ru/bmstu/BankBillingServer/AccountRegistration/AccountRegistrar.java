package ru.bmstu.BankBillingServer.AccountRegistration;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

public class AccountRegistrar extends AbstractActor {

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(AccountMessage.class, msg -> {
                    System.out.println(msg.getAccountID());
                    sender().tell("GOOD", self());
                })
                .build();

    }
}
