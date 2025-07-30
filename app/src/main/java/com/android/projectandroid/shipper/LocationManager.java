package com.android.projectandroid.shipper;

import static com.android.projectandroid.data.userModel.ApiService.apiService;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.android.projectandroid.R;
import com.android.projectandroid.data.orderModel.CancelOrderRequest;
import com.android.projectandroid.data.orderModel.GetOrderDetailResponse;
import com.android.projectandroid.data.orderModel.SimpleOrder;
import com.android.projectandroid.data.orderModel.SimpleOrderApiService;
import com.android.projectandroid.data.orderModel.UpdateOrderResponse;
import com.android.projectandroid.data.orderModel.UpdateOrderStatusRequest;
import com.android.projectandroid.data.userModel.NormalUser;
import com.android.projectandroid.data.userModel.PreferenceManager;
import com.android.projectandroid.model.Location;
import com.android.projectandroid.network.ApiClient;
import com.android.projectandroid.network.ApiService;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// LocationManager là lớp quản lý vị trí, bản đồ, marker, dẫn đường, cảm biến la bàn
public class LocationManager implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener, SensorEventListener {
    private Context context; // Context của ứng dụng
    private GoogleMap mMap; // Đối tượng GoogleMap để thao tác bản đồ
    private List<Location> allLocations = new ArrayList<>(); // Danh sách toàn bộ địa điểm lấy từ backend
    private List<String> regionIds = new ArrayList<>(); // Danh sách ID vùng khu vực hiện có
    private Map<String, String> regionNames = new HashMap<>(); // Map vùng: regionId -> tên vùng
    private String currentFilterRegionId = null; // ID vùng hiện tại đang lọc (null là không lọc)
    private static final String GEOAPIFY_API_KEY = "d540009157e44dafb4a24c91563f8109"; // API key Geoapify dùng để lấy tuyến đường
    private LatLng currentLocation; // Vị trí hiện tại của shipper
    private LatLng previousLocation; // Vị trí trước đó, dùng để tính bearing
    private Marker userLocationMarker; // Marker biểu diễn vị trí người dùng trên bản đồ
    private float lastUsedBearing = 0f; // Góc hướng la bàn cuối cùng đã dùng để tránh cập nhật quá nhỏ

    // Các biến liên quan đến điều hướng (navigation)
    private boolean isNavigating = false; // Cờ trạng thái đang dẫn đường
    private Marker destinationMarker = null; // Marker điểm đến (đích) khi dẫn đường
    private Handler navigationHandler = new Handler(Looper.getMainLooper()); // Handler chạy các tác vụ định kỳ trên thread UI
    private Runnable navigationRunnable; // Runnable cập nhật lộ trình định kỳ
    private static final int NAVIGATION_UPDATE_INTERVAL = 5000; // Tần suất cập nhật lộ trình mỗi 5 giây
    private Polyline currentRoute = null; // Polyline hiện tại thể hiện tuyến đường đang đi
    private Map<Marker, Location> markerLocationMap = new HashMap<>(); // Map từ Marker sang Location tương ứng
    private List<Polyline> routePolylines = new ArrayList<>(); // Danh sách các polyline đang vẽ trên bản đồ
    private List<Marker> routeMarkers = new ArrayList<>(); // Danh sách marker đánh số thứ tự trên tuyến đường

    // Biến quản lý camera tracking nâng cao
    private boolean isRouteMode = false; // Cờ chế độ theo dõi tuyến đường (route mode)
    private boolean shouldTrackLocation = false; // Có nên theo dõi vị trí người dùng để cập nhật camera không
    private boolean shouldFollowBearing = false; // Có nên xoay bản đồ theo hướng la bàn hay hướng di chuyển
    private float currentBearing = 0f; // Góc hướng hiện tại dùng để làm mượt góc
    private Handler cameraTrackingHandler = new Handler(Looper.getMainLooper()); // Handler chạy các tác vụ camera tracking
    private Runnable cameraTrackingRunnable; // Runnable cập nhật camera định kỳ
    private static final int CAMERA_TRACKING_INTERVAL = 500; // Tần suất cập nhật camera mỗi 0.5 giây
    private static final float BEARING_THRESHOLD = 5f; // Ngưỡng góc thay đổi tối thiểu để update camera tránh rung lắc
    private static final float ROUTE_TRACKING_ZOOM = 18f; // Mức zoom khi theo dõi tuyến đường
    private static final float ROUTE_TRACKING_TILT = 60f; // Góc nghiêng camera khi xem 3D
    private static final float BEARING_SMOOTHING_FACTOR = 0.3f; // Hệ số làm mượt góc (lerp)

    // Các biến cảm biến để đọc dữ liệu la bàn (compass)
    private SensorManager sensorManager;
    private Sensor accelerometer; // Cảm biến gia tốc kế
    private Sensor magnetometer;  // Cảm biến từ trường (la bàn)
    private float[] gravity;      // Mảng lưu dữ liệu gia tốc
    private float[] geomagnetic;  // Mảng lưu dữ liệu từ trường
    private float currentAzimuth = 0f; // Góc hướng la bàn hiện tại
    private boolean compassEnabled = false; // Cờ bật/tắt la bàn
    private PreferenceManager preferenceManager;

    // Handler để chạy tác vụ trên luồng chính (UI thread)
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // Interface callback khi danh sách location được tải xong
    public interface LocationFetchCallback {
        void onLocationsFetched(List<Location> locations);
    }

    // Interface callback nâng cao cho trạng thái điều hướng
    public interface NavigationCallback {
        void onNavigationStarted(Marker destination);
        void onNavigationEnded();
        void onNavigationUpdated();
        void onRouteCalculated(); // Khi tuyến đường đã được tính toán xong
    }

    private NavigationCallback navigationCallback; // Biến lưu callback đăng ký bên ngoài

    // Constructor
    public LocationManager(Context context, GoogleMap map) {
        preferenceManager = new PreferenceManager(context);

        this.context = context;
        this.mMap = map;

        // Khởi tạo sensor manager và lấy cảm biến gia tốc kế, từ trường
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        // Đặt vị trí mặc định ban đầu để tránh null
        this.currentLocation = new LatLng(10.797449, 106.655479);
        this.previousLocation = this.currentLocation;

        // Đăng ký InfoWindowAdapter và sự kiện click cửa sổ thông tin marker
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(this);

        // Khởi tạo map vùng với tên vùng tương ứng
        initializeRegionNames();

        // Khởi tạo runnable camera tracking
        initializeCameraTracking();
    }

    // Xử lý sự kiện khi cảm biến thay đổi
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Nếu la bàn không bật hoặc không ở chế độ route thì bỏ qua
        if (!compassEnabled || !isRouteMode) return;

        // Lấy dữ liệu gia tốc kế
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone();
        }
        // Lấy dữ liệu cảm biến từ trường
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone();
        }

        // Khi đã có đủ dữ liệu gia tốc và từ trường, tính ma trận xoay để lấy góc la bàn
        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];

            // Tính ma trận xoay (rotation matrix)
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                // Lấy góc azimuth (hướng Bắc la bàn) từ ma trận xoay
                float azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                // Làm mượt giá trị góc la bàn để tránh nhảy giật
                currentAzimuth = smoothBearing(currentAzimuth, azimuth);

                // Cập nhật lại camera theo hướng la bàn mới
                updateCameraWithCompass();
            }
        }
    }

    // Bật cảm biến la bàn
    private void enableCompass() {
        compassEnabled = true;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    // Tắt cảm biến la bàn
    private void disableCompass() {
        compassEnabled = false;
        sensorManager.unregisterListener(this);
    }

    // Cập nhật vị trí camera trên bản đồ theo hướng la bàn
    private void updateCameraWithCompass() {
        if (mMap == null || currentLocation == null || !compassEnabled) return;

        // Chỉ update khi góc la bàn thay đổi lớn hơn 2 độ để tránh lag UI
        if (Math.abs(currentAzimuth - lastUsedBearing) < 2f) return;

        lastUsedBearing = currentAzimuth;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocation)      // Tập trung camera vào vị trí hiện tại
                .zoom(ROUTE_TRACKING_ZOOM)   // Zoom bản đồ
                .bearing(currentAzimuth)      // Xoay theo hướng la bàn
                .tilt(ROUTE_TRACKING_TILT)   // Nghiêng góc camera để nhìn 3D
                .build();

        // Thực hiện hiệu ứng di chuyển camera mượt
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 200, null);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Có thể xử lý nếu cần, hiện tại bỏ trống
    }

    // Đăng ký callback cho navigation
    public void setNavigationCallback(NavigationCallback callback) {
        this.navigationCallback = callback;
    }

    // Khởi tạo tên các vùng (region)
    private void initializeRegionNames() {
        // Thêm region id và tên hiển thị tương ứng (có thể fetch từ server nếu cần)
        regionNames.put("region1", "District 1");
        regionNames.put("region2", "District 2");
        regionNames.put("region3", "District 3");
        // Có thể thêm vùng khác tại đây
    }

    // Khởi tạo runnable dùng cho camera tracking định kỳ
    private void initializeCameraTracking() {
        cameraTrackingRunnable = new Runnable() {
            @Override
            public void run() {
                // Nếu đang tracking vị trí và có vị trí hiện tại
                if (shouldTrackLocation && currentLocation != null && (isNavigating || isRouteMode)) {
                    // Luôn gọi hàm cập nhật camera để xử lý đúng góc
                    updateCameraPositionWithTracking();
                }

                // Nếu vẫn đang tracking, schedule lại runnable tiếp theo sau 500ms
                if (shouldTrackLocation) {
                    cameraTrackingHandler.postDelayed(this, CAMERA_TRACKING_INTERVAL);
                }
            }
        };
    }

    // Cập nhật camera theo tracking vị trí và góc hướng
    private void updateCameraPositionWithTracking() {
        if (mMap == null || currentLocation == null) return;

        float bearingToUse = 0f;   // Góc hướng camera cần dùng
        boolean shouldUseBearing = false; // Cờ dùng góc hay không

        // Nếu la bàn bật và có góc la bàn hợp lệ
        if (compassEnabled && currentAzimuth != 0) {
            bearingToUse = currentAzimuth; // Dùng góc la bàn
            shouldUseBearing = true;
        }
        // Nếu la bàn tắt nhưng có vị trí trước đó và bật theo dõi hướng
        else if (previousLocation != null && shouldFollowBearing) {
            // Tính góc di chuyển từ vị trí trước đến hiện tại
            float newBearing = calculateMovementBearing(previousLocation, currentLocation);
            if (newBearing != -1) {
                // Làm mượt góc
                currentBearing = smoothBearing(currentBearing, newBearing);
                bearingToUse = currentBearing;
                shouldUseBearing = true;
            }
        }

        // Nếu không có góc hợp lệ, giữ nguyên góc camera hiện tại
        if (!shouldUseBearing) {
            CameraPosition currentCameraPosition = mMap.getCameraPosition();
            bearingToUse = currentCameraPosition.bearing;
        }

        // Xây dựng camera position mới với các tham số zoom, góc nghiêng, hướng
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocation)
                .zoom(ROUTE_TRACKING_ZOOM)
                .bearing(bearingToUse) // Góc hướng camera
                .tilt(shouldFollowBearing || compassEnabled ? ROUTE_TRACKING_TILT : 0) // Góc nghiêng nếu bật tracking hướng
                .build();

        // Di chuyển camera mượt đến vị trí mới
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null);
    }

    // Tính góc hướng di chuyển giữa 2 điểm
    private float calculateMovementBearing(LatLng from, LatLng to) {
        if (from == null || to == null) return -1;

        // Nếu di chuyển quá nhỏ (<2m) thì không tính để tránh rung lắc
        double distance = calculateDistance(from, to);
        if (distance < 2) return -1; // < 2 mét

        // Tính toán góc theo công thức Haversine
        double lat1 = Math.toRadians(from.latitude);
        double lat2 = Math.toRadians(to.latitude);
        double deltaLng = Math.toRadians(to.longitude - from.longitude);

        double x = Math.sin(deltaLng) * Math.cos(lat2);
        double y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLng);

        double bearing = Math.toDegrees(Math.atan2(x, y));
        return (float) ((bearing + 360) % 360);
    }

    // Hàm làm mượt góc hướng (lerp vòng)
    private float smoothBearing(float current, float target) {
        float diff = target - current;
        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;

        return current + BEARING_SMOOTHING_FACTOR * diff;
    }

    // Bắt đầu vòng lặp camera tracking
    private void startCameraTracking() {
        shouldTrackLocation = true;
        shouldFollowBearing = true;

        cameraTrackingHandler.removeCallbacks(cameraTrackingRunnable);
        cameraTrackingHandler.post(cameraTrackingRunnable);
    }

    // Dừng vòng lặp camera tracking và reset camera
    private void stopCameraTracking() {
        shouldTrackLocation = false;
        shouldFollowBearing = false;

        disableCompass(); // Tắt cảm biến la bàn

        cameraTrackingHandler.removeCallbacks(cameraTrackingRunnable);

        resetCameraView();
    }

    // Reset camera về vị trí và góc mặc định (zoom 16, bearing 0, tilt 0)
    private void resetCameraView() {
        if (mMap != null && currentLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentLocation)
                    .zoom(16f)
                    .bearing(0)
                    .tilt(0)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500, null);
        }
    }

    // Lấy danh sách location từ API backend
    public void fetchAllLocations(final LocationFetchCallback callback) {
        ApiService apiService = ApiClient.getApiService();

        // Gọi API Retrofit lấy danh sách location
        retrofit2.Call<LocationResponse> call = apiService.getLocations();
        call.enqueue(new retrofit2.Callback<LocationResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LocationResponse> call, retrofit2.Response<LocationResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    allLocations = response.body().getData();
                    processLocations(allLocations);

                    if (callback != null) {
                        callback.onLocationsFetched(allLocations);
                    }
                } else {
                    showToast("Không thể tải danh sách địa điểm");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<LocationResponse> call, Throwable t) {
                showToast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Xử lý danh sách location để thêm marker lên bản đồ, áp dụng bộ lọc
    private void processLocations(List<Location> locations) {
        mMap.clear(); // Xóa hết marker, polyline cũ trên bản đồ
        markerLocationMap.clear(); // Xóa map cũ

        // Thêm marker vị trí hiện tại của người dùng (màu xanh)
        updateUserMarkerPosition(currentLocation);

        // Duyệt danh sách location
        for (Location location : locations) {
            // Nếu đang lọc vùng và location không cùng vùng thì bỏ qua
            if (currentFilterRegionId != null && !location.getRegionId().equals(currentFilterRegionId)) {
                continue;
            }

            // Lấy tọa độ
            double lat = location.getPosition().getLat();
            double lng = location.getPosition().getLng();
            LatLng position = new LatLng(lat, lng);

            if (location.isActive())
            {
                // Thêm marker mới lên bản đồ
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(location.getOrderId())
                        .snippet(location.getAddress()));
                if (marker != null) {
                    // Lưu map từ marker sang location để dùng sau
                    markerLocationMap.put(marker, location);
                }

            }

            // Thu thập regionId để dùng cho bộ lọc vùng
            if (!regionIds.contains(location.getRegionId())) {
                regionIds.add(location.getRegionId());
            }
        }

        // Nếu đang điều hướng thì cập nhật lại tuyến đường
        if (isNavigating && destinationMarker != null) {
            updateNavigationRoute();
        }
    }

    // Cập nhật vị trí marker người dùng trên bản đồ
    public void updateUserMarkerPosition(LatLng newPosition) {
        if (newPosition == null) return;

        previousLocation = currentLocation; // Lưu vị trí trước đó để tính hướng
        currentLocation = newPosition;

        if (userLocationMarker != null) {
            userLocationMarker.remove(); // Xóa marker cũ
        }

        // Thêm marker mới tại vị trí hiện tại, màu xanh
        userLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // Nếu đang bật tracking thì cập nhật lại camera theo vị trí và hướng
        if (shouldTrackLocation && (isNavigating || isRouteMode)) {
            updateCameraPositionWithTracking();
        }
    }

    // Trả về trạng thái camera đang tracking hay không
    public boolean isCameraTracking() {
        return shouldTrackLocation && (isNavigating || isRouteMode);
    }

    // Bộ lọc các location theo vùng (regionId)
    public void filterByRegion(String regionId) {
        currentFilterRegionId = regionId;
        processLocations(allLocations);
    }

    // Hiển thị dialog để người dùng chọn vùng cần lọc hoặc bỏ lọc
    public void toggleRegionFilter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Lọc theo Khu vực");

        // Tạo danh sách ánh xạ regionId -> tên hiển thị
        Map<String, String> regionNames = new HashMap<>();
        regionNames.put("68356a5ed01f28b34beb8c66", "Quận 1, TP Hồ Chí Minh");
        regionNames.put("68356a5ed01f28b34beb8c67", "Quận 3, TP Hồ Chí Minh");
        regionNames.put("68356a5ed01f28b34beb8c68", "Quận 5, TP Hồ Chí Minh");
        regionNames.put("68356a5ed01f28b34beb8c69", "Quận 7, TP Hồ Chí Minh");
        regionNames.put("68356a5ed01f28b34beb8c6a", "Quận Bình Thạnh, TP Hồ Chí Minh");
        regionNames.put("68356a5ed01f28b34beb8c6b", "Quận Thủ Đức, TP Hồ Chí Minh");
        regionNames.put("68356a5ed01f28b34beb8c6c", "Thành phố Thủ Dầu Một, Bình Dương");
        regionNames.put("68356a5ed01f28b34beb8c6d", "Thành phố Dĩ An, Bình Dương");
        regionNames.put("68356a5ed01f28b34beb8c6e", "Thành phố Thuận An, Bình Dương");
        regionNames.put("68356a5ed01f28b34beb8c6f", "Thành phố Tân Uyên, Bình Dương");

        // Tạo danh sách tên hiển thị theo thứ tự từ regionIds
        final String[] regionDisplayNames = new String[regionIds.size()];
        for (int i = 0; i < regionIds.size(); i++) {
            String regionId = regionIds.get(i);
            regionDisplayNames[i] = regionNames.containsKey(regionId) ?
                    regionNames.get(regionId) : "Khu vực " + regionId;
        }

        builder.setItems(regionDisplayNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedRegionId = regionIds.get(which);
                filterByRegion(selectedRegionId);
            }
        });

        // Nút trung lập để bỏ lọc, hiển thị tất cả
        builder.setNeutralButton("Hiển thị Tất cả", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filterByRegion(null);
            }
        });

        builder.show();
    }

    // Tìm tuyến đường ngắn nhất (TSP) từ vị trí bắt đầu đi qua tất cả các địa điểm trong danh sách
    public void findShortestRoute(LatLng startLocation) {
        List<Location> locationsToVisit = new ArrayList<>();

        // Lọc danh sách location theo vùng (nếu có)
        for (Location location : allLocations) {
            if (currentFilterRegionId == null || location.getRegionId().equals(currentFilterRegionId)) {
                locationsToVisit.add(location);
            }
        }

        if (locationsToVisit.isEmpty()) {
            showToast("Không có địa điểm nào để ghé thăm");
            return;
        }

        // Bật chế độ route mode và camera tracking, bật la bàn
        isRouteMode = true;
        startCameraTracking();
        enableCompass();

        // Giới hạn số địa điểm tối đa tránh quá tải API định tuyến
        final int MAX_LOCATIONS = 10;
        if (locationsToVisit.size() > MAX_LOCATIONS) {
            // Chỉ lấy MAX_LOCATIONS điểm gần nhất
            locationsToVisit = findClosestLocations(startLocation, locationsToVisit, MAX_LOCATIONS);
            showToast("Giới hạn lộ trình đến " + MAX_LOCATIONS + " địa điểm gần nhất");
        }

        // Giải bài toán TSP để tìm tuyến đường tối ưu nhất
        List<LatLng> optimalRoute = solveTSP(startLocation, locationsToVisit);
        List<Location> orderedLocations = getOrderedLocations(optimalRoute, locationsToVisit);

        // Xóa các polyline, marker tuyến đường cũ
        clearRouteElements();

        // Thêm marker vị trí bắt đầu (người dùng)
        updateUserMarkerPosition(startLocation);

        // Thêm các marker đánh số thứ tự trên tuyến đường
        addNumberedMarkers(orderedLocations);

        // Vẽ tuyến đường nối các điểm lần lượt, với delay để tránh gọi API quá nhanh
        drawRoutesSequentially(optimalRoute, 0);

        // Gọi callback thông báo tuyến đường đã được tính
        if (navigationCallback != null) {
            navigationCallback.onRouteCalculated();
        }

        showToast("Chế độ theo dõi lộ trình được kích hoạt");
    }

    // Tìm ra các địa điểm gần nhất dựa trên khoảng cách từ điểm bắt đầu
    private List<Location> findClosestLocations(LatLng startPoint, List<Location> locations, int maxCount) {
        Map<Location, Double> locationDistances = new HashMap<>();
        for (Location location : locations) {
            LatLng locationPos = new LatLng(
                    location.getPosition().getLat(),
                    location.getPosition().getLng()
            );
            double distance = calculateDistance(startPoint, locationPos);
            locationDistances.put(location, distance);
        }

        // Sắp xếp location theo khoảng cách tăng dần
        List<Location> sortedLocations = new ArrayList<>(locations);
        Collections.sort(sortedLocations, new Comparator<Location>() {
            @Override
            public int compare(Location loc1, Location loc2) {
                return Double.compare(locationDistances.get(loc1), locationDistances.get(loc2));
            }
        });

        // Trả về danh sách maxCount địa điểm gần nhất
        return sortedLocations.subList(0, Math.min(maxCount, sortedLocations.size()));
    }

    // Vẽ các đoạn tuyến đường nối tiếp nhau lần lượt với delay 1 giây giữa mỗi đoạn
    private void drawRoutesSequentially(final List<LatLng> optimalRoute, final int index) {
        if (index >= optimalRoute.size() - 1) {
            // Vẽ xong hết đoạn, không cần vẽ đoạn cuối quay về điểm đầu
            showToast("Hoàn tất lập kế hoạch lộ trình");
            return;
        }

        // Gọi hàm lấy dữ liệu và vẽ đoạn đường từ điểm index đến index+1
        fetchAndDrawRoute(optimalRoute.get(index), optimalRoute.get(index + 1), index == optimalRoute.size() - 2);

        // Lên lịch vẽ đoạn tiếp theo sau 1 giây delay
        mainHandler.postDelayed(() -> {
            drawRoutesSequentially(optimalRoute, index + 1);
        }, 1000);
    }

    // Xóa hết các route polyline và marker đánh số tuyến đường cũ
    private void clearRouteElements() {
        for (Polyline polyline : routePolylines) {
            polyline.remove();
        }
        routePolylines.clear();

        for (Marker marker : routeMarkers) {
            marker.remove();
        }
        routeMarkers.clear();

        // Xóa bản đồ nhưng vẫn giữ marker vị trí người dùng
        mMap.clear();
        markerLocationMap.clear();
    }

    // Thêm marker đánh số thứ tự theo danh sách location đã sắp xếp
    private void addNumberedMarkers(List<Location> orderedLocations) {
        for (int i = 0; i < orderedLocations.size(); i++) {
            Location location = orderedLocations.get(i);
            LatLng position = new LatLng(
                    location.getPosition().getLat(),
                    location.getPosition().getLng()
            );

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title((i + 1) + ". " + location.getName()) // Tiêu đề có số thứ tự
                    .snippet(location.getAddress()));

            if (marker != null) {
                routeMarkers.add(marker);
                markerLocationMap.put(marker, location);
            }
        }
    }

    // Dựa trên danh sách LatLng tối ưu, tìm lại thứ tự location tương ứng
    private List<Location> getOrderedLocations(List<LatLng> optimalRoute, List<Location> locations) {
        List<Location> orderedLocations = new ArrayList<>();

        // Bỏ qua vị trí đầu (start location)
        for (int i = 1; i < optimalRoute.size(); i++) {
            LatLng point = optimalRoute.get(i);

            for (Location location : locations) {
                LatLng locationPos = new LatLng(
                        location.getPosition().getLat(),
                        location.getPosition().getLng()
                );

                if (isSameLocation(point, locationPos, 0.00001)) {
                    orderedLocations.add(location);
                    break;
                }
            }
        }

        return orderedLocations;
    }

    // Kiểm tra 2 vị trí có gần nhau không (theo độ sai số tolerance)
    private boolean isSameLocation(LatLng point1, LatLng point2, double tolerance) {
        return Math.abs(point1.latitude - point2.latitude) < tolerance &&
                Math.abs(point1.longitude - point2.longitude) < tolerance;
    }

    // Giải thuật TSP (Held-Karp) để tìm tuyến đường ngắn nhất đi qua tất cả điểm
    private List<LatLng> solveTSP(LatLng startLocation, List<Location> locations) {
        int n = locations.size();
        if (n == 0) return new ArrayList<>();

        // Tạo ma trận khoảng cách (kích thước n+1, bao gồm điểm bắt đầu)
        double[][] dist = new double[n + 1][n + 1];
        List<LatLng> points = new ArrayList<>();
        points.add(startLocation);
        for (Location loc : locations) {
            points.add(new LatLng(loc.getPosition().getLat(), loc.getPosition().getLng()));
        }

        // Tính khoảng cách giữa các điểm
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= n; j++) {
                dist[i][j] = calculateDistance(points.get(i), points.get(j));
            }
        }

        int size = 1 << n; // 2^n trạng thái
        double[][] dp = new double[size][n]; // Bảng DP lưu chi phí tối thiểu
        int[][] parent = new int[size][n];   // Bảng lưu nút cha để truy vết đường đi

        // Khởi tạo giá trị max cho dp
        for (int i = 0; i < size; i++)
            for (int j = 0; j < n; j++)
                dp[i][j] = Double.MAX_VALUE;

        // Khởi tạo base case: đi thẳng từ điểm start đến điểm i
        for (int i = 0; i < n; i++) {
            dp[1 << i][i] = dist[0][i + 1];
        }

        // Duyệt tất cả trạng thái mask
        for (int mask = 0; mask < size; mask++) {
            for (int u = 0; u < n; u++) {
                if ((mask & (1 << u)) == 0) continue;
                for (int v = 0; v < n; v++) {
                    if ((mask & (1 << v)) != 0 || u == v) continue;
                    int next = mask | (1 << v);
                    double newDist = dp[mask][u] + dist[u + 1][v + 1];
                    if (newDist < dp[next][v]) {
                        dp[next][v] = newDist;
                        parent[next][v] = u;
                    }
                }
            }
        }

        // Tìm chi phí tối thiểu và điểm cuối cùng của chu trình
        double minCost = Double.MAX_VALUE;
        int lastIndex = -1;
        for (int i = 0; i < n; i++) {
            double cost = dp[size - 1][i] + dist[i + 1][0];
            if (cost < minCost) {
                minCost = cost;
                lastIndex = i;
            }
        }

        // Truy vết đường đi từ bảng parent
        List<Integer> path = new ArrayList<>();
        int mask = size - 1;
        while (mask != 0) {
            path.add(lastIndex);
            int temp = mask;
            mask ^= (1 << lastIndex);
            lastIndex = parent[temp][lastIndex];
        }
        Collections.reverse(path);

        // Tạo danh sách tuyến đường LatLng theo thứ tự
        List<LatLng> route = new ArrayList<>();
        route.add(startLocation);
        for (int idx : path) {
            route.add(points.get(idx + 1));
        }
        route.add(startLocation); // Quay về điểm đầu

        return route;
    }

    // Tính khoảng cách giữa 2 điểm LatLng (Haversine)
    private double calculateDistance(LatLng point1, LatLng point2) {
        double lat1 = point1.latitude;
        double lon1 = point1.longitude;
        double lat2 = point2.latitude;
        double lon2 = point2.longitude;

        final int R = 6371; // Bán kính Trái Đất (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000; // Đổi sang mét
    }

    // Gọi API Geoapify lấy tuyến đường giữa 2 điểm và vẽ trên bản đồ
    public void fetchAndDrawRoute(LatLng startPoint, LatLng endPoint, final boolean isLastSegment) {
        if (startPoint == null || endPoint == null) {
            Log.e("RouteFetch", "Invalid points: start or end point is null");
            showToast("Lỗi: Điểm lộ trình không hợp lệ");
            return;
        }

        if (!isValidCoordinate(startPoint) || !isValidCoordinate(endPoint)) {
            Log.e("RouteFetch", "Invalid coordinates outside allowed range");
            showToast("Lỗi: Phạm vi tọa độ không hợp lệ");
            return;
        }

        // Định dạng tọa độ đúng chuẩn (6 chữ số thập phân)
        String formattedStart = String.format(Locale.US, "%.6f,%.6f", startPoint.latitude, startPoint.longitude);
        String formattedEnd = String.format(Locale.US, "%.6f,%.6f", endPoint.latitude, endPoint.longitude);

        // Tạo URL API Geoapify
        String url = String.format(
                Locale.US,
                "https://api.geoapify.com/v1/routing?waypoints=%s|%s&mode=drive&apiKey=%s",
                formattedStart,
                formattedEnd,
                GEOAPIFY_API_KEY
        );

        Log.d("RouteFetch", "Fetching route: " + url);

        // Tạo OkHttp client với timeout 15s
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        // Thực hiện gọi API bất đồng bộ
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("RouteFetch", "Failed to fetch route: " + e.getMessage());
                mainHandler.post(() -> showToast("Không thể tải lộ trình: Lỗi kết nối"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    parseAndDrawRoute(jsonData, startPoint, endPoint, isLastSegment);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e("RouteFetch", "API error: " + response.code() + " - " + errorBody);
                    mainHandler.post(() -> showToast("API error: " + response.code()));

                    // Xử lý các lỗi API cụ thể
                    if (response.code() == 400) {
                        Log.e("RouteFetch", "Bad Request - Check API key and parameters");
                    } else if (response.code() == 401) {
                        Log.e("RouteFetch", "Unauthorized - Check API key");
                    } else if (response.code() == 429) {
                        Log.e("RouteFetch", "Too many requests - Rate limit exceeded");
                    }
                }
            }
        });
    }

    // Kiểm tra tọa độ hợp lệ (latitude [-90, 90], longitude [-180, 180])
    private boolean isValidCoordinate(LatLng point) {
        return point.latitude >= -90 && point.latitude <= 90 &&
                point.longitude >= -180 && point.longitude <= 180;
    }

    /**
     * Phân tích dữ liệu JSON trả về từ Geoapify, vẽ tuyến đường trên bản đồ
     */
    private void parseAndDrawRoute(String jsonData, final LatLng startPoint, final LatLng endPoint, final boolean isLastSegment) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray features = jsonObject.getJSONArray("features");

            if (features.length() > 0) {
                JSONObject feature = features.getJSONObject(0);
                JSONObject geometry = feature.getJSONObject("geometry");

                if (geometry.getString("type").equals("MultiLineString")) {
                    JSONArray coordinateArrays = geometry.getJSONArray("coordinates");
                    final List<LatLng> routePoints = new ArrayList<>();

                    // Xử lý từng đoạn polyline con trong MultiLineString
                    for (int i = 0; i < coordinateArrays.length(); i++) {
                        JSONArray lineCoordinates = coordinateArrays.getJSONArray(i);

                        // Xử lý từng điểm trong đoạn
                        for (int j = 0; j < lineCoordinates.length(); j++) {
                            JSONArray point = lineCoordinates.getJSONArray(j);
                            double lng = point.getDouble(0); // Lưu ý GeoJSON: [longitude, latitude]
                            double lat = point.getDouble(1);

                            routePoints.add(new LatLng(lat, lng));
                        }
                    }

                    // Chạy UI thread để vẽ
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            drawRoute(routePoints, isLastSegment);

                            // Zoom để xem toàn tuyến nếu đây là đoạn cuối
                            if (isLastSegment) {
                                if (!isRouteMode) {
                                    List<LatLng> allPoints = new ArrayList<>();
                                    for (Polyline polyline : routePolylines) {
                                        allPoints.addAll(polyline.getPoints());
                                    }
                                    zoomToShowRoute(allPoints);
                                }
                            }

                            if (isNavigating && navigationCallback != null) {
                                navigationCallback.onNavigationUpdated();
                            }
                        }
                    });

                    JSONObject properties = feature.getJSONObject("properties");
                    int distance = properties.getInt("distance");
                    double time = properties.getDouble("time");
                    Log.d("RouteFetch", String.format("Route fetched: %.1f km, %.1f minutes",
                            distance / 1000.0, time / 60.0));
                }
            }
        } catch (JSONException e) {
            Log.e("RouteFetch", "Error parsing response: " + e.getMessage());
            e.printStackTrace();
            showToast("Lỗi phân tích dữ liệu lộ trình");
        }
    }

    /**
     * Vẽ tuyến đường trên bản đồ
     */
    private void drawRoute(List<LatLng> routePoints, boolean isLastSegment) {
        // Nếu đang dẫn đường và có route hiện tại thì xóa route cũ
        if (isNavigating && currentRoute != null) {
            currentRoute.remove();
        }

        // Màu xanh cho các đoạn trung gian, màu xanh lá cho đoạn cuối
        int color = isLastSegment ? Color.GREEN : Color.BLUE;

        PolylineOptions polylineOptions = new PolylineOptions()
                .color(color)
                .width(8);

        for (LatLng point : routePoints) {
            polylineOptions.add(point);
        }

        Polyline polyline = mMap.addPolyline(polylineOptions);
        routePolylines.add(polyline);

        if (isNavigating) {
            currentRoute = polyline;
        }
    }

    /**
     * Zoom để hiển thị toàn bộ tuyến đường trên bản đồ
     */
    private void zoomToShowRoute(List<LatLng> routePoints) {
        if (routePoints.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng point : routePoints) {
            boundsBuilder.include(point);
        }

        LatLngBounds bounds = boundsBuilder.build();

        int padding = 100; // padding pixels

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cameraUpdate);
    }

    /**
     * Làm mới bản đồ (refresh) bằng cách xử lý lại locations
     */
    public void refreshMap() {
        processLocations(allLocations);
    }

    /**
     * Cập nhật vị trí hiện tại mới của người dùng
     */
    public void updateCurrentLocation(LatLng newLocation) {
        if (newLocation != null) {
            this.currentLocation = newLocation;
            updateUserMarkerPosition(newLocation);

            if (isNavigating && destinationMarker != null) {
                updateNavigationRoute();
            }
        }
    }

    /**
     * Bắt đầu dẫn đường đến một marker cụ thể
     */
    public void startNavigation(Marker marker) {
        if (marker == null) return;

        destinationMarker = marker;
        isNavigating = true;

        updateNavigationRoute();

        if (navigationCallback != null) {
            navigationCallback.onNavigationStarted(marker);
        }
    }

    /**
     * Dừng dẫn đường hiện tại
     */
    public void stopNavigation() {
        if (!isNavigating) return;

        isNavigating = false;
        destinationMarker = null;
        isRouteMode = false;

        stopCameraTracking();

        if (navigationRunnable != null) {
            navigationHandler.removeCallbacks(navigationRunnable);
        }

        if (currentRoute != null) {
            currentRoute.remove();
            currentRoute = null;
        }

        refreshMap();

        if (navigationCallback != null) {
            navigationCallback.onNavigationEnded();
        }
    }

    /**
     * Cập nhật lại tuyến đường dẫn
     */
    private void updateNavigationRoute() {
        if (!isNavigating || destinationMarker == null || currentLocation == null) {
            return;
        }

        if (isOffRoute(currentLocation)) {
            // Nếu đi sai đường, thông báo và tính lại tuyến mới
            showToast("Bạn đã đi sai đường, đang cập nhật lộ trình mới...");
            fetchAndDrawRoute(currentLocation, destinationMarker.getPosition(), true);
        } else {
            // Nếu vẫn đúng đường, vẽ lại tuyến hiện tại
            fetchAndDrawRoute(currentLocation, destinationMarker.getPosition(), true);
        }

        // Lên lịch cập nhật lại định kỳ
        navigationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isNavigating) {
                    updateNavigationRoute();
                }
            }
        };
        navigationHandler.postDelayed(navigationRunnable, NAVIGATION_UPDATE_INTERVAL);
    }

    /**
     * InfoWindowAdapter - trả về null để dùng khung mặc định
     */
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * InfoWindowAdapter - nội dung của cửa sổ thông tin marker
     */
    @Override
    public View getInfoContents(Marker marker) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);

        TextView titleTextView = view.findViewById(R.id.text_title);
        TextView snippetTextView = view.findViewById(R.id.text_snippet);
//        Button btnClose = view.findViewById(R.id.btn_close);
//        Button btnNavigate = view.findViewById(R.id.btn_navigate);

        titleTextView.setText(marker.getTitle());

        Location location = markerLocationMap.get(marker);
        if (location != null) {
            StringBuilder infoBuilder = new StringBuilder(marker.getSnippet());

            if (location.getContactPhone() != null && !location.getContactPhone().isEmpty()) {
                infoBuilder.append("\nPhone: ").append(location.getContactPhone());
            }

            if (location.getDescription() != null && !location.getDescription().isEmpty()) {
                infoBuilder.append("\nDescription: ").append(location.getDescription());
            }

            snippetTextView.setText(infoBuilder.toString());
        } else {
            snippetTextView.setText(marker.getSnippet());
        }

        return view;
    }

    // Ngưỡng khoảng cách tối đa shipper được phép lệch khỏi tuyến đường (mét)
    private static final double MAX_DISTANCE_FROM_ROUTE_METERS = 30;

    // Kiểm tra xem vị trí hiện tại có nằm ngoài phạm vi tuyến đường cho phép không
    private boolean isOffRoute(LatLng currentPos) {
        if (currentRoute == null) return false;

        List<LatLng> routePoints = currentRoute.getPoints();
        if (routePoints == null || routePoints.size() < 2) return false;

        // Kiểm tra khoảng cách từ điểm đến từng đoạn tuyến trên đường
        for (int i = 0; i < routePoints.size() - 1; i++) {
            LatLng segmentStart = routePoints.get(i);
            LatLng segmentEnd = routePoints.get(i + 1);

            double distanceToSegment = distanceToLineSegment(currentPos, segmentStart, segmentEnd);
            if (distanceToSegment <= MAX_DISTANCE_FROM_ROUTE_METERS) {
                return false; // Vẫn còn trong phạm vi tuyến đường
            }
        }
        return true; // Lệch tuyến đường
    }

    /**
     * Tính khoảng cách ngắn nhất từ điểm đến đoạn thẳng nối 2 điểm trên bản đồ (dùng SphericalUtil)
     */
    private double distanceToLineSegment(LatLng point, LatLng segmentStart, LatLng segmentEnd) {
        if (segmentStart.equals(segmentEnd)) {
            return SphericalUtil.computeDistanceBetween(point, segmentStart);
        }

        // Vector hóa tọa độ latitude, longitude
        double lat1 = segmentStart.latitude;
        double lng1 = segmentStart.longitude;
        double lat2 = segmentEnd.latitude;
        double lng2 = segmentEnd.longitude;
        double lat3 = point.latitude;
        double lng3 = point.longitude;

        double dx = lat2 - lat1;
        double dy = lng2 - lng1;

        // Tham số t xác định điểm chiếu của point trên đoạn thẳng
        double t = ((lat3 - lat1) * dx + (lng3 - lng1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t)); // Giới hạn trong đoạn thẳng [0,1]

        double projLat = lat1 + t * dx;
        double projLng = lng1 + t * dy;

        LatLng projectedPoint = new LatLng(projLat, projLng);

        // Khoảng cách từ điểm đến điểm chiếu
        return SphericalUtil.computeDistanceBetween(point, projectedPoint);
    }

    /**
     * Xử lý sự kiện click vào cửa sổ thông tin marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        showMarkerOptionsDialog(marker);
    }

    /**
     * Hiển thị dialog với các tùy chọn cho marker (Đóng hoặc Dẫn đường)
     */
    // 2. Cập nhật method showMarkerOptionsDialog
    private void showMarkerOptionsDialog(final Marker marker) {
        Location location = markerLocationMap.get(marker);
        if (location == null) {
            return;
        }

        // Kiểm tra xem location có orderId không
        if (location.getOrderId() != null && !location.getOrderId().isEmpty()) {
            // Có orderId -> hiển thị thông tin order
            showOrderDetailsDialog(marker, location);
        } else {
            // Không có orderId -> hiển thị dialog cũ
            showBasicMarkerDialog(marker);
        }
    }

    // 3. Method hiển thị dialog cơ bản (không có order)
    private void showBasicMarkerDialog(final Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(marker.getTitle());

        String[] options = {"Đóng"};

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                        marker.hideInfoWindow();
            }
        });

        builder.show();
    }

    // 4. Method hiển thị dialog với thông tin order
    private void showOrderDetailsDialog(final Marker marker, Location location) {
        // Tạo progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Đang tải thông tin đơn hàng...");
        progressDialog.show();

        Log.e("", "showMarkerOptionsDialog: "+ location.getOrderId());
        // Gọi API lấy thông tin order
        SimpleOrderApiService.orderApiService.getOrderById(location.getOrderId())
                .enqueue(new retrofit2.Callback<GetOrderDetailResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<GetOrderDetailResponse> call, retrofit2.Response<GetOrderDetailResponse> response) {
                        progressDialog.dismiss();

                        GetOrderDetailResponse test = response.body();
                        if (response.isSuccessful() && response.body() != null) {
                            GetOrderDetailResponse orderResponse = response.body();
                            if (orderResponse.isSuccess()) {
                                showOrderInfoDialog(marker, location, orderResponse.getData());
                            } else {
                                showErrorDialog("Không thể tải thông tin đơn hàng: " + orderResponse.getMessage());
                                Log.e("","Không thể tải thông tin đơn hàng: " + orderResponse.getMessage());

                            }
                        } else {
                            showErrorDialog("Lỗi kết nối server");
                            Log.e("","Lỗi kết nối server");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<GetOrderDetailResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        showErrorDialog("Lỗi kết nối: " + t.getMessage());
                        Log.e("","Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    // 5. Method hiển thị dialog với thông tin chi tiết order
    private void showOrderInfoDialog(final Marker marker, Location location, SimpleOrder order) {
        // Tạo custom layout cho dialog
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_order_details, null);

        // Ánh xạ các view
        ImageView ivProductImage = dialogView.findViewById(R.id.iv_product_image_dialog);
        TextView tvProductName = dialogView.findViewById(R.id.tv_product_name);
        TextView tvCustomerName = dialogView.findViewById(R.id.tv_customer_name);
        TextView tvCustomerPhone = dialogView.findViewById(R.id.tv_customer_phone);
        TextView tvAddress = dialogView.findViewById(R.id.tv_address);
        TextView tvPrice = dialogView.findViewById(R.id.tv_price);
        TextView tvOrderType = dialogView.findViewById(R.id.tv_order_type);
        TextView tvStatus = dialogView.findViewById(R.id.tv_status);
        TextView tvNotes = dialogView.findViewById(R.id.tv_notes);

        // Điền thông tin vào các view
        fillOrderInfo(order, ivProductImage, tvProductName, tvCustomerName,
                tvCustomerPhone, tvAddress, tvPrice, tvOrderType, tvStatus, tvNotes);

        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Thông tin đơn hàng")
                .setView(dialogView)
                .setPositiveButton("Lấy hàng", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cập nhật trạng thái đơn hàng thành "delivered"
                        updateOrderStatusToDelivered(order.getId(), marker);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Chưa thể lấy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cập nhật trạng thái đơn hàng thành "cancelled"
                        updateOrderStatusToCancelled(order.getId(), marker);
                        dialog.dismiss();
                    }
                })
                .show();
    }
    // Method để cập nhật trạng thái đơn hàng thành "delivered"
    private void updateOrderStatusToDelivered(String orderId, Marker marker) {
        // Tạo request body
        NormalUser currentuser = preferenceManager.getNormalUser();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus("delivered");
        request.setNotes("Đã lấy hàng thành công từ ứng dụng");
        request.setShipperId(currentuser.get_id());

        // Gọi API
        SimpleOrderApiService.orderApiService.updateOrderStatus(orderId, request).enqueue(new retrofit2.Callback<UpdateOrderResponse>() {
            @Override
            public void onResponse(retrofit2.Call<UpdateOrderResponse> call, retrofit2.Response<UpdateOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UpdateOrderResponse updateResponse = response.body();

                    if (updateResponse.isSuccess()) {
                        // Lấy thông tin đơn hàng đã cập nhật từ response
                        UpdateOrderResponse.OrderData updatedOrder = updateResponse.getData();

                        // Hiển thị toast thành công
                        Toast.makeText(context, "Đã cập nhật trạng thái giao hàng thành công",
                                Toast.LENGTH_LONG).show();
                        String timestamp = updatedOrder.getUpdatedAt();

                        // Tạo thông báo giao hàng thành công
                        if (updatedOrder != null) {
                            String deliveryAddress = updatedOrder.getCustomerAddress();
                            NotificationManager.getInstance().addSuccessDeliveryNotification(
                                    orderId,
                                    deliveryAddress,
                                    timestamp
                            );
                        }

                        // Ẩn marker hoặc cập nhật trạng thái marker
                        marker.setVisible(false); // hoặc cập nhật icon marker
                        marker.hideInfoWindow();


                    } else {
                        Toast.makeText(context, "Lỗi: " + updateResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Không thể cập nhật trạng thái đơn hàng",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UpdateOrderResponse> call, Throwable t) {
                Log.e("OrderUpdate", "Error updating order status to delivered", t);
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method để cập nhật trạng thái đơn hàng thành "cancelled"
    private void updateOrderStatusToCancelled(String orderId, Marker marker) {
        // Tạo request body cho cancel
        NormalUser currentuser = preferenceManager.getNormalUser();
        CancelOrderRequest cancelRequest = new CancelOrderRequest();
        cancelRequest.setCancelReason("Chưa thể lấy hàng tại thời điểm này");
        cancelRequest.setShipperId(currentuser.get_id());

        // Gọi API cancel
        SimpleOrderApiService.orderApiService.cancelOrder(orderId, cancelRequest).enqueue(new retrofit2.Callback<UpdateOrderResponse>() {
            @Override
            public void onResponse(retrofit2.Call<UpdateOrderResponse> call, retrofit2.Response<UpdateOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UpdateOrderResponse cancelResponse = response.body();

                    if (cancelResponse.isSuccess()) {
                        // Lấy thông tin đơn hàng từ response
                        UpdateOrderResponse.OrderData cancelledOrder = cancelResponse.getData();

                        String timestamp = cancelledOrder.getUpdatedAt();
                        Toast.makeText(context, "Đã hủy đơn hàng thành công",
                                Toast.LENGTH_LONG).show();

                        // Tạo thông báo lấy hàng thất bại (vì đã hủy)
                        if (cancelledOrder != null) {
                            String pickupAddress = cancelledOrder.getCustomerAddress();
                            NotificationManager.getInstance().addFailedPickupNotification(
                                    orderId,
                                    pickupAddress,
                                    "Đơn hàng đã được hủy",
                                    timestamp
                            );
                        }

                        // Ẩn marker sau khi hủy đơn hàng
                        marker.setVisible(false);
                        marker.hideInfoWindow();

                    } else {
                        Toast.makeText(context, "Lỗi: " + cancelResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Không thể hủy đơn hàng",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UpdateOrderResponse> call, Throwable t) {
                Log.e("OrderUpdate", "Error cancelling order", t);
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Helper methods để lấy địa chỉ từ response data

    private String getAddressFromMarker(Marker marker) {
        // Lấy địa chỉ từ marker title hoặc snippet
        if (marker.getTitle() != null) {
            return marker.getTitle();
        } else if (marker.getSnippet() != null) {
            return marker.getSnippet();
        }
        return "Địa điểm không xác định";
    }

    // 6. Method điền thông tin order vào các view
    private void fillOrderInfo(SimpleOrder order, ImageView ivProductImage, TextView tvProductName,
                               TextView tvCustomerName, TextView tvCustomerPhone, TextView tvAddress,
                               TextView tvPrice, TextView tvOrderType, TextView tvStatus, TextView tvNotes) {

        // Thông tin sản phẩm
        if (order.getProductSnapshot() != null) {
            tvProductName.setText(order.getProductSnapshot().getProductName());

            // Load hình ảnh sản phẩm
            if (order.getProductSnapshot().getProductImage() != null &&
                    !order.getProductSnapshot().getProductImage().isEmpty()) {
                Glide.with(context)
                        .load(order.getProductSnapshot().getProductImage())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.warnning_red_2)
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            tvProductName.setText("Không có thông tin sản phẩm");
            ivProductImage.setImageResource(R.drawable.ic_placeholder);
        }

        // Thông tin khách hàng
        if (order.getCustomerInfo() != null) {
            tvCustomerName.setText(order.getCustomerName());
            tvCustomerPhone.setText(order.getCustomerInfo().getPhoneNumber());
            tvAddress.setText(order.getCustomerInfo().getAddress());
        } else {
            tvCustomerName.setText("Không có thông tin");
            tvCustomerPhone.setText("Không có thông tin");
            tvAddress.setText("Không có thông tin");
        }

        // Giá tiền
        if (order.getOrderType() != null && order.getOrderType().equals("donation_pickup")) {
            tvPrice.setText("Quyên góp: " + (order.getKg() != null ? order.getKg() + " kg" : "0 kg"));
        } else {
            tvPrice.setText(formatPrice(order.getPrice()) + " VNĐ");
        }

        // Loại đơn hàng
        tvOrderType.setText(getOrderTypeDisplayText(order.getOrderType()));

        // Trạng thái
        tvStatus.setText(getStatusDisplayText(order.getStatus()));

        // Ghi chú
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            tvNotes.setText(order.getNotes());
            tvNotes.setVisibility(View.VISIBLE);
        } else {
            tvNotes.setVisibility(View.GONE);
        }
    }

    // 7. Utility methods
    private String formatPrice(double price) {
        return String.format("%,.0f", price);
    }

    private String getOrderTypeDisplayText(String orderType) {
        if (orderType == null) return "Không xác định";

        switch (orderType) {
            case "pass_pickup":
                return "Đi lấy đồ pass";
            case "donation_pickup":
                return "Đi lấy đồ quyên góp";
            case "delivery":
                return "Đi giao hàng";
            default:
                return "Không xác định";
        }
    }

    private String getStatusDisplayText(String status) {
        if (status == null) return "Không xác định";

        switch (status) {
            case "shipping":
                return "Đang giao";
            case "delivered":
                return "Đã giao";
            case "cancelled":
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(context)
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Hiển thị Toast trên UI thread
     */
    private void showToast(final String message) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
