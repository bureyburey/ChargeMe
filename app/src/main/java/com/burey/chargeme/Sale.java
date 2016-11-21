package com.burey.chargeme;

/**
 * Created by burey on 10/07/2016.
 */
public class Sale implements Comparable{
    private int _item_id;
    private int _client_id;
    private String itemName;
    private String clientName;
    private int ordered;


    public Sale(int _item_id, int _client_id, String itemName, String clientName, int ordered) {
        this._item_id = _item_id;
        this._client_id = _client_id;
        this.itemName = itemName;
        this.clientName = clientName;
        this.ordered = ordered;
    }

    public int get_item_id() {
        return _item_id;
    }

    public void set_item_id(int _item_id) {
        this._item_id = _item_id;
    }

    public int get_client_id() {
        return _client_id;
    }

    public void set_client_id(int _client_id) {
        this._client_id = _client_id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public int getOrdered() {
        return ordered;
    }

    public void setOrdered(int ordered) {
        this.ordered = ordered;
    }

    @Override
    public int compareTo(Object another) {
        Sale other = (Sale) another;
        return new Integer(other.getOrdered()).compareTo(new Integer(getOrdered()));
    }
}
