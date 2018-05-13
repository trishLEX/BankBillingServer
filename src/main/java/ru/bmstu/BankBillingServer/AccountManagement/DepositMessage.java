package ru.bmstu.BankBillingServer.AccountManagement;

public class DepositMessage {
    private int accountID;
    private float sum;

    public DepositMessage(int accountID, float sum) {
        this.accountID = accountID;
        this.sum = sum;
    }

    public int getAccountID() {
        return accountID;
    }

    public float getSum() {
        return sum;
    }
}
