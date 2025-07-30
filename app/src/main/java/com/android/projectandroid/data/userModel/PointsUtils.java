package com.android.projectandroid.data.userModel;

/**
 * Utility class cho xử lý điểm thưởng và level system
 */
public class PointsUtils {

    // ✅ CONSTANTS
    public static final int POINTS_PER_KG = 1000;

    // Level thresholds
    public static final int BRONZE_THRESHOLD = 5000;
    public static final int SILVER_THRESHOLD = 20000;
    public static final int GOLD_THRESHOLD = 50000;
    public static final int DIAMOND_THRESHOLD = 100000;

    // Level colors
    public static final int COLOR_BEGINNER = 0xFF757575;   // Grey
    public static final int COLOR_BRONZE = 0xFFCD7F32;     // Bronze
    public static final int COLOR_SILVER = 0xFFC0C0C0;     // Silver
    public static final int COLOR_GOLD = 0xFFFFD700;       // Gold
    public static final int COLOR_DIAMOND = 0xFF00BCD4;    // Cyan

    // Private constructor to prevent instantiation
    private PointsUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ✅ POINTS CALCULATION METHODS

    /**
     * Tính điểm từ kg quyên góp
     * @param kg Số kg dưới dạng string
     * @return Số điểm tương ứng
     */
    public static int calculatePointsFromKg(String kg) {
        try {
            double kgValue = Double.parseDouble(kg);
            return (int) Math.floor(kgValue * POINTS_PER_KG);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Tính điểm từ kg quyên góp
     * @param kg Số kg dưới dạng double
     * @return Số điểm tương ứng
     */
    public static int calculatePointsFromKg(double kg) {
        if (kg < 0) return 0;
        return (int) Math.floor(kg * POINTS_PER_KG);
    }

    /**
     * Tính kg cần thiết để đạt được số điểm
     * @param points Số điểm mục tiêu
     * @return Số kg cần quyên góp
     */
    public static double calculateKgForPoints(int points) {
        if (points <= 0) return 0.0;
        return Math.ceil((double) points / POINTS_PER_KG);
    }

    // ✅ FORMATTING METHODS

    /**
     * Format điểm thành chuỗi đẹp
     * @param points Số điểm
     * @return Chuỗi đã format (VD: "15.5K", "2.3M")
     */
    public static String formatPoints(int points) {
        if (points >= 1000000) {
            return String.format("%.1fM", points / 1000000.0);
        } else if (points >= 1000) {
            return String.format("%.1fK", points / 1000.0);
        } else {
            return String.valueOf(points);
        }
    }

    /**
     * Format kg với đơn vị
     * @param kg Số kg
     * @return Chuỗi đã format (VD: "2.5 kg")
     */
    public static String formatKg(double kg) {
        if (kg == (int) kg) {
            return String.format("%d kg", (int) kg);
        } else {
            return String.format("%.1f kg", kg);
        }
    }

    /**
     * Format kg từ string với đơn vị
     * @param kg Số kg dưới dạng string
     * @return Chuỗi đã format
     */
    public static String formatKg(String kg) {
        try {
            double kgValue = Double.parseDouble(kg);
            return formatKg(kgValue);
        } catch (NumberFormatException e) {
            return kg + " kg";
        }
    }

    // ✅ LEVEL SYSTEM METHODS

    /**
     * Lấy level từ điểm
     * @param points Số điểm hiện tại
     * @return Tên level
     */
    public static String getLevelFromPoints(int points) {
        if (points >= DIAMOND_THRESHOLD) {
            return "Diamond";
        } else if (points >= GOLD_THRESHOLD) {
            return "Gold";
        } else if (points >= SILVER_THRESHOLD) {
            return "Silver";
        } else if (points >= BRONZE_THRESHOLD) {
            return "Bronze";
        } else {
            return "Beginner";
        }
    }

    /**
     * Lấy màu cho level
     * @param level Tên level
     * @return Mã màu
     */
    public static int getColorForLevel(String level) {
        switch (level) {
            case "Diamond":
                return COLOR_DIAMOND;
            case "Gold":
                return COLOR_GOLD;
            case "Silver":
                return COLOR_SILVER;
            case "Bronze":
                return COLOR_BRONZE;
            default:
                return COLOR_BEGINNER;
        }
    }

    /**
     * Lấy màu cho level từ điểm
     * @param points Số điểm hiện tại
     * @return Mã màu
     */
    public static int getColorForPoints(int points) {
        return getColorForLevel(getLevelFromPoints(points));
    }

    /**
     * Tính phần trăm tiến độ đến level tiếp theo
     * @param currentPoints Điểm hiện tại
     * @return Phần trăm (0-100)
     */
    public static int getProgressToNextLevel(int currentPoints) {
        String currentLevel = getLevelFromPoints(currentPoints);

        switch (currentLevel) {
            case "Beginner":
                return Math.min(100, (currentPoints * 100) / BRONZE_THRESHOLD);
            case "Bronze":
                return Math.min(100, ((currentPoints - BRONZE_THRESHOLD) * 100) / (SILVER_THRESHOLD - BRONZE_THRESHOLD));
            case "Silver":
                return Math.min(100, ((currentPoints - SILVER_THRESHOLD) * 100) / (GOLD_THRESHOLD - SILVER_THRESHOLD));
            case "Gold":
                return Math.min(100, ((currentPoints - GOLD_THRESHOLD) * 100) / (DIAMOND_THRESHOLD - GOLD_THRESHOLD));
            case "Diamond":
                return 100; // Max level
            default:
                return 0;
        }
    }

    /**
     * Lấy điểm cần thiết cho level tiếp theo
     * @param currentPoints Điểm hiện tại
     * @return Số điểm cần thiết
     */
    public static int getPointsNeededForNextLevel(int currentPoints) {
        String currentLevel = getLevelFromPoints(currentPoints);

        switch (currentLevel) {
            case "Beginner":
                return BRONZE_THRESHOLD - currentPoints;
            case "Bronze":
                return SILVER_THRESHOLD - currentPoints;
            case "Silver":
                return GOLD_THRESHOLD - currentPoints;
            case "Gold":
                return DIAMOND_THRESHOLD - currentPoints;
            case "Diamond":
                return 0; // Max level
            default:
                return BRONZE_THRESHOLD;
        }
    }

    /**
     * Lấy tên level tiếp theo
     * @param currentPoints Điểm hiện tại
     * @return Tên level tiếp theo
     */
    public static String getNextLevel(int currentPoints) {
        String currentLevel = getLevelFromPoints(currentPoints);

        switch (currentLevel) {
            case "Beginner":
                return "Bronze";
            case "Bronze":
                return "Silver";
            case "Silver":
                return "Gold";
            case "Gold":
                return "Diamond";
            case "Diamond":
                return "Diamond"; // Max level
            default:
                return "Bronze";
        }
    }

    /**
     * Kiểm tra có level up không
     * @param oldPoints Điểm cũ
     * @param newPoints Điểm mới
     * @return true nếu level up
     */
    public static boolean isLevelUp(int oldPoints, int newPoints) {
        return !getLevelFromPoints(oldPoints).equals(getLevelFromPoints(newPoints));
    }

    /**
     * Lấy thông báo level up
     * @param oldPoints Điểm cũ
     * @param newPoints Điểm mới
     * @return Thông báo level up hoặc chuỗi rỗng
     */
    public static String getLevelUpMessage(int oldPoints, int newPoints) {
        String oldLevel = getLevelFromPoints(oldPoints);
        String newLevel = getLevelFromPoints(newPoints);

        if (!oldLevel.equals(newLevel)) {
            return "🎉 Chúc mừng! Bạn đã lên cấp " + newLevel + "!";
        }
        return "";
    }

    // ✅ VALIDATION METHODS

    /**
     * Validate kg input
     * @param kg Số kg dưới dạng string
     * @return true nếu hợp lệ
     */
    public static boolean isValidKg(String kg) {
        try {
            double kgValue = Double.parseDouble(kg);
            return kgValue > 0 && kgValue <= 1000; // Giới hạn tối đa 1000kg
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Lấy thông báo lỗi cho kg không hợp lệ
     * @param kg Số kg dưới dạng string
     * @return Thông báo lỗi hoặc null nếu hợp lệ
     */
    public static String getKgValidationError(String kg) {
        if (kg == null || kg.trim().isEmpty()) {
            return "Vui lòng nhập số kg";
        }

        try {
            double kgValue = Double.parseDouble(kg);
            if (kgValue <= 0) {
                return "Số kg phải lớn hơn 0";
            }
            if (kgValue > 1000) {
                return "Số kg không được vượt quá 1000";
            }
            return null; // Hợp lệ
        } catch (NumberFormatException e) {
            return "Số kg không hợp lệ";
        }
    }

    /**
     * Validate points input
     * @param points Số điểm
     * @return true nếu hợp lệ
     */
    public static boolean isValidPoints(int points) {
        return points >= 0 && points <= 10000000; // Giới hạn tối đa 10M điểm
    }

    // ✅ INFORMATION METHODS

    /**
     * Lấy thông tin chi tiết về level
     * @param level Tên level
     * @return Thông tin chi tiết
     */
    public static String getLevelDescription(String level) {
        switch (level) {
            case "Beginner":
                return "Người mới bắt đầu - Hãy quyên góp để tích lũy điểm!";
            case "Bronze":
                return "Đồng - Bạn đã có những đóng góp đầu tiên cho cộng đồng";
            case "Silver":
                return "Bạc - Một người quyên góp tích cực và có ảnh hưởng";
            case "Gold":
                return "Vàng - Một tấm gương sáng trong việc chia sẻ yêu thương";
            case "Diamond":
                return "Kim cương - Người đóng góp xuất sắc nhất cho cộng đồng";
            default:
                return "Không xác định";
        }
    }

    /**
     * Lấy emoji cho level
     * @param level Tên level
     * @return Emoji tương ứng
     */
    public static String getLevelEmoji(String level) {
        switch (level) {
            case "Beginner":
                return "🌱";
            case "Bronze":
                return "🥉";
            case "Silver":
                return "🥈";
            case "Gold":
                return "🥇";
            case "Diamond":
                return "💎";
            default:
                return "❓";
        }
    }

    /**
     * Lấy tất cả thông tin level
     * @return Array chứa thông tin tất cả level
     */
    public static LevelInfo[] getAllLevels() {
        return new LevelInfo[]{
                new LevelInfo("Beginner", 0, BRONZE_THRESHOLD - 1, COLOR_BEGINNER, "🌱"),
                new LevelInfo("Bronze", BRONZE_THRESHOLD, SILVER_THRESHOLD - 1, COLOR_BRONZE, "🥉"),
                new LevelInfo("Silver", SILVER_THRESHOLD, GOLD_THRESHOLD - 1, COLOR_SILVER, "🥈"),
                new LevelInfo("Gold", GOLD_THRESHOLD, DIAMOND_THRESHOLD - 1, COLOR_GOLD, "🥇"),
                new LevelInfo("Diamond", DIAMOND_THRESHOLD, Integer.MAX_VALUE, COLOR_DIAMOND, "💎")
        };
    }

    /**
     * Class chứa thông tin chi tiết của một level
     */
    public static class LevelInfo {
        public final String name;
        public final int minPoints;
        public final int maxPoints;
        public final int color;
        public final String emoji;

        public LevelInfo(String name, int minPoints, int maxPoints, int color, String emoji) {
            this.name = name;
            this.minPoints = minPoints;
            this.maxPoints = maxPoints;
            this.color = color;
            this.emoji = emoji;
        }

        public String getDisplayName() {
            return emoji + " " + name;
        }

        public String getPointRange() {
            if (maxPoints == Integer.MAX_VALUE) {
                return formatPoints(minPoints) + "+";
            }
            return formatPoints(minPoints) + " - " + formatPoints(maxPoints);
        }

        @Override
        public String toString() {
            return getDisplayName() + " (" + getPointRange() + ")";
        }
    }
}