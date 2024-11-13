package org.example;

import com.google.gson.annotations.SerializedName;

public class PlaceSchedule {
    @SerializedName("id")
    private String id;

    @SerializedName("availability")
    private int availability;

    @SerializedName("datetime")
    private String datetime;

    @SerializedName("price")
    private int price;

    // Getters e Setters
    public String getId() { return id; }
    public int getAvailability() { return availability; }
    public String getDatetime() { return datetime; }
    public int getPrice() { return price; }
}
