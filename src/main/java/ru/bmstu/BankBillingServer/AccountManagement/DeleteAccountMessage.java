package ru.bmstu.BankBillingServer.AccountManagement;

public class DeleteAccountMessage {
    private int accountID;

    public DeleteAccountMessage(int accountID) {
        this.accountID = accountID;
    }

    public int getAccountID() {
        return accountID;
    }
}
