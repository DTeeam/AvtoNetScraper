package org.example;

import java.util.Date;

public class Post {
    private String title;
    private double price ;
    private int modelYear;
    private int mileage;
    private int power;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public int getModelYear() {
        return modelYear;
    }

    public void setModelYear(int modelYear) {
        this.modelYear = modelYear;
    }

    @Override
    public String toString() {
        return "Post{" +
                "title='" + title + '\'' +
                ", price=" + price +
                ", modelYear=" + modelYear +
                ", mileage=" + mileage +
                ", power=" + power +
                ", url='" + url + '\'' +
                '}';
    }
}
