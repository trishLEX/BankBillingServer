package ru.bmstu.BankBillingServer.AccountManagement;

public class CreateAccountMessage {
    private int accountID;

    public CreateAccountMessage(int accountID) {
        this.accountID = accountID;
    }

    public int getAccountID() {
        return accountID;
    }
}
