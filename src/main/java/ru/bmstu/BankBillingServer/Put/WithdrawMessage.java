package ru.bmstu.BankBillingServer.Put;

public class WithdrawMessage {
    private int accountID;
    private float sum;

    public WithdrawMessage(int accountID, float sum) {
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
