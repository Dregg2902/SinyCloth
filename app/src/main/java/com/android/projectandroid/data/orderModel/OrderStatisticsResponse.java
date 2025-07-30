package com.android.projectandroid.data.orderModel;

public class OrderStatisticsResponse {
    private boolean success;
    private String message;
    private OrderStatistics data;

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OrderStatistics getData() {
        return data;
    }

    public void setData(OrderStatistics data) {
        this.data = data;
    }

    // Inner class for statistics data
    public static class OrderStatistics {
        private Overview overview;
        private ByStatus byStatus;
        private ByStatus.DonationStats donation; // ✅ THÊM THỐNG KÊ QUYÊN GÓP

        public ByStatus.DonationStats getDonation() { return donation; }


        public Overview getOverview() {
            return overview;
        }

        public void setOverview(Overview overview) {
            this.overview = overview;
        }

        public ByStatus getByStatus() {
            return byStatus;
        }

        public void setByStatus(ByStatus byStatus) {
            this.byStatus = byStatus;
        }

        public static class Overview {
            private int totalOrders;
            private double totalValue;
            private String shippingRate;
            private String deliveryRate;
            private String cancellationRate;

            // Getters and Setters
            public int getTotalOrders() {
                return totalOrders;
            }

            public void setTotalOrders(int totalOrders) {
                this.totalOrders = totalOrders;
            }

            public double getTotalValue() {
                return totalValue;
            }

            public void setTotalValue(double totalValue) {
                this.totalValue = totalValue;
            }

            public String getShippingRate() {
                return shippingRate;
            }

            public void setShippingRate(String shippingRate) {
                this.shippingRate = shippingRate;
            }

            public String getDeliveryRate() {
                return deliveryRate;
            }

            public void setDeliveryRate(String deliveryRate) {
                this.deliveryRate = deliveryRate;
            }

            public String getCancellationRate() {
                return cancellationRate;
            }

            public void setCancellationRate(String cancellationRate) {
                this.cancellationRate = cancellationRate;
            }
        }

        public static class ByStatus {
            private StatusInfo shipping;
            private StatusInfo delivered;
            private StatusInfo cancelled;

            public StatusInfo getShipping() {
                return shipping;
            }

            public void setShipping(StatusInfo shipping) {
                this.shipping = shipping;
            }

            public StatusInfo getDelivered() {
                return delivered;
            }

            public void setDelivered(StatusInfo delivered) {
                this.delivered = delivered;
            }

            public StatusInfo getCancelled() {
                return cancelled;
            }

            public void setCancelled(StatusInfo cancelled) {
                this.cancelled = cancelled;
            }

            public static class StatusInfo {
                private int count;
                private double totalValue;

                public int getCount() {
                    return count;
                }

                public void setCount(int count) {
                    this.count = count;
                }

                public double getTotalValue() {
                    return totalValue;
                }

                public void setTotalValue(double totalValue) {
                    this.totalValue = totalValue;
                }
            }
            // ✅ THỐNG KÊ QUYÊN GÓP
            public static class DonationStats {
                private int totalDonationOrders;
                private double totalKgDonated;
                private int totalPointsAwarded;
                private String averageKgPerOrder;

                public int getTotalDonationOrders() { return totalDonationOrders; }
                public double getTotalKgDonated() { return totalKgDonated; }
                public int getTotalPointsAwarded() { return totalPointsAwarded; }
                public String getAverageKgPerOrder() { return averageKgPerOrder; }
            }
        }
    }
}