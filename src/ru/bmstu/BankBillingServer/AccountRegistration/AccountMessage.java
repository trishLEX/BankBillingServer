package ru.bmstu.BankBillingServer.AccountRegistration;

public class AccountMessage {
    private int accountID;

    public AccountMessage(int accountID) {
        this.accountID = accountID;
    }

    public int getAccountID() {
        return accountID;
    }
}
