package ru.bmstu.BankBillingServer.Get;

public class GetBalanceMessage {
    private int accountID;

    public GetBalanceMessage(int accountID) {
        this.accountID = accountID;
    }

    public int getAccountID() {
        return accountID;
    }
}
