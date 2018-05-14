package ru.bmstu.BankBillingServer.Delete;

public class DeleteAccountMessage {
    private int accountID;

    public DeleteAccountMessage(int accountID) {
        this.accountID = accountID;
    }

    public int getAccountID() {
        return accountID;
    }
}
