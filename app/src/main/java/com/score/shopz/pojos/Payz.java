package com.score.shopz.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * POJO to keep pay attributes
 *
 * @author eranga bandara (erangaeb@gmail.com)
 */
public class Payz implements Parcelable {
    String account;
    String amount;
    String time;

    public Payz(String account, String amount, String time) {
        this.account = account;
        this.amount = amount;
        this.time = time;
    }

    protected Payz(Parcel in) {
        account = in.readString();
        amount = in.readString();
        time = in.readString();
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public static final Creator<Payz> CREATOR = new Creator<Payz>() {
        @Override
        public Payz createFromParcel(Parcel in) {
            return new Payz(in);
        }

        @Override
        public Payz[] newArray(int size) {
            return new Payz[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(account);
        dest.writeString(amount);
        dest.writeString(time);
    }
}
