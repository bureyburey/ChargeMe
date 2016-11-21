package com.burey.chargeme;

/**
 * Created by burey on 30/08/2015.
 */

public class Item  implements Comparable{
    private int _id;
    private String name;
    private String quantity;
    private String price;
    private int ordered;

    public Item(int _id, String name, String quantity, String price) {
        this._id = _id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.ordered = 0;
    }

    public int getOrdered() {
        return ordered;
    }

    public void setOrdered(int ordered) {
        this.ordered = ordered;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public int compareTo(Object another) {
        Item other = (Item) another;
        return new Integer(other.getOrdered()).compareTo(new Integer(getOrdered()));
    }
}