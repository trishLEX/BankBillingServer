package ru.bmstu.BankBillingServer.Post;

public class CreateAccountMessage {
    private int accountID;

    public CreateAccountMessage(int accountID) {
        this.accountID = accountID;
    }

    public int getAccountID() {
        return accountID;
    }
}
