package ru.bmstu.BankBillingServer.AccountsEntity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "accounts", schema = "billing_schema", catalog = "billingdb")
public class AccountsEntity {
    private int id;
    private double money;

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "money", nullable = false, precision = 0)
    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountsEntity that = (AccountsEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, money);
    }
}
