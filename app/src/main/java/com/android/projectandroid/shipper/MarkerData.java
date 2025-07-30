package com.android.projectandroid.shipper;

import com.android.projectandroid.data.orderModel.SimpleOrder;
import com.android.projectandroid.model.Location;

public class MarkerData {
    public Location location;
    public SimpleOrder order;

    public MarkerData(Location location, SimpleOrder order) {
        this.location = location;
        this.order = order;
    }
}

