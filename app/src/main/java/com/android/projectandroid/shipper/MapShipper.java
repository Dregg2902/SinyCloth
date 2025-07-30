package com.android.projectandroid.shipper;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.android.projectandroid.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapShipper extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap; // Biến GoogleMap dùng để thao tác bản đồ
    private FusedLocationProviderClient fusedLocationClient; // Dùng lấy vị trí GPS chính xác
    private ImageButton btnFilter, btnZoomIn, btnZoomOut; // Các nút bấm trên giao diện
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1; // Mã request quyền truy cập vị trí

    private LocationManager locationManager; // Quản lý vị trí và các marker, dẫn đường (custom class)
    private LatLng currentUserLocation;  // Vị trí hiện tại của user (dạng LatLng)
    private LatLng previousUserLocation; // Vị trí trước đó dùng để tính hướng di chuyển
    private boolean isNavigating = false; // Cờ trạng thái đang dẫn đường
    private boolean isRouteMode = false;  // Cờ trạng thái đang theo dõi lộ trình (route mode)

    private LocationCallback locationCallback; // Callback nhận vị trí khi update GPS
    private Marker destinationMarker; // Marker điểm đến khi dẫn đường

    // Lưu map giữa các marker trên bản đồ với đối tượng Location model (để lấy dữ liệu thêm khi cần)
    private Map<Marker, com.android.projectandroid.model.Location> markerLocationMap = new HashMap<>();

    // Handler & Runnable cho việc cập nhật dẫn đường định kỳ
    private Handler navigationHandler = new Handler(Looper.getMainLooper());
    private Runnable navigationRunnable;
    private static final int NAVIGATION_UPDATE_INTERVAL = 3000; // 3 giây cập nhật dẫn đường

    // Cập nhật vị trí GPS với tần số 1 giây (theo dõi chính xác hơn)
    private static final int LOCATION_UPDATE_INTERVAL = 1000;

    // THÊM CÁC BIẾN MỚI VÀO ĐẦU CLASS
    private float currentBearing = 0f; // Hướng hiện tại từ GPS
    private float previousBearing = 0f; // Hướng trước đó để làm mượt
    private boolean hasValidBearing = false; // Có hướng hợp lệ không

    // Thêm low-pass filter để làm mượt dữ liệu sensor
    private static final float ALPHA = 0.15f;
    private float[] filteredAccelerometer = new float[3];
    private float[] filteredMagnetometer = new float[3];

    // Các biến quản lý camera theo dõi vị trí và hướng (tracking)
    private boolean shouldTrackLocation = false;  // Có nên theo dõi vị trí user không?
    private boolean shouldFollowBearing = false;  // Có nên xoay theo hướng la bàn không?
    private Handler cameraUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable cameraUpdateRunnable;
    private static final int CAMERA_UPDATE_INTERVAL = 500; // 0.5 giây cập nhật camera

    // Ngưỡng thay đổi góc để update camera (tránh rung lắc khi la bàn thay đổi nhỏ)
    private static final float BEARING_THRESHOLD = 5f;

    // Mức zoom mặc định khi theo dõi vị trí
    private static final float TRACKING_ZOOM_LEVEL = 18f;

    // Góc nghiêng camera cho chế độ xem 3D
    private static final float TILT_ANGLE = 60f;

    // Yêu cầu cập nhật vị trí chính xác cao cho FusedLocationProvider
    private LocationRequest highAccuracyLocationRequest;

    // Constructor mặc định
    public MapShipper() {
    }

    // Factory method tạo fragment mới nếu cần truyền tham số
    public static MapShipper newInstance(String param1, String param2) {
        MapShipper fragment = new MapShipper();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    // Inflate layout fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_shipper, container, false);

        // Đảm bảo padding cho view theo hệ thống (status bar, nav bar) động
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.mapShipper), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        return view;
    }

    // Sau khi view được tạo xong
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Khởi tạo client lấy vị trí GPS chính xác
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Tạo cấu hình yêu cầu cập nhật vị trí với độ chính xác cao
        createHighAccuracyLocationRequest();

        // Khởi tạo các nút filter, zoom và thiết lập sự kiện bấm
        btnFilter = view.findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> onFilterButtonClick());

        btnZoomIn = view.findViewById(R.id.btn_zoom_in);
        btnZoomIn.setOnClickListener(v -> zoomIn());

        btnZoomOut = view.findViewById(R.id.btn_zoom_out);
        btnZoomOut.setOnClickListener(v -> zoomOut());

        // Lấy SupportMapFragment con từ XML và đăng ký callback khi map sẵn sàng
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_fragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // Sẽ gọi onMapReady()
        }

        // Tạo callback nhận vị trí cập nhật từ GPS
        createLocationCallback();
    }

    // Tạo đối tượng LocationRequest để yêu cầu vị trí chính xác cao, update mỗi 1 giây
    private void createHighAccuracyLocationRequest() {
        highAccuracyLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // GPS cao cấp
                .setInterval(LOCATION_UPDATE_INTERVAL) // Yêu cầu mỗi 1 giây
                .setFastestInterval(LOCATION_UPDATE_INTERVAL / 2); // Tần suất nhanh nhất 0.5 giây
    }

    // Tạo callback nhận dữ liệu vị trí khi GPS cập nhật
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (android.location.Location location : locationResult.getLocations()) {
                    previousUserLocation = currentUserLocation;
                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // TÍNH TOÁN HƯỚNG DI CHUYỂN TỪ GPS
                    if (location.hasBearing()) {
                        // Sử dụng bearing từ GPS nếu có
                        float newBearing = location.getBearing();
                        updateBearing(newBearing);
                    } else if (previousUserLocation != null && currentUserLocation != null) {
                        // Tính bearing từ 2 điểm GPS liên tiếp
                        float calculatedBearing = calculateBearing(previousUserLocation, currentUserLocation);
                        if (calculatedBearing >= 0) {
                            updateBearing(calculatedBearing);
                        }
                    }

                    // Cập nhật vị trí hiện tại trong locationManager
                    if (locationManager != null) {
                        locationManager.updateCurrentLocation(currentUserLocation);
                    }

                    // Cập nhật vị trí marker người dùng
                    updateUserMarkerPosition();

                    // Cập nhật camera nếu đang theo dõi
                    if (shouldTrackLocation && (isNavigating || isRouteMode)) {
                        updateCameraPosition();
                    }

                    // Cập nhật navigation route
                    if (isNavigating && destinationMarker != null) {
                        updateNavigationRoute();
                    }
                }
            }
        };
    }

    // THÊM PHƯƠNG THỨC MỚI ĐỂ CẬP NHẬT BEARING
    private void updateBearing(float newBearing) {
        if (!hasValidBearing) {
            currentBearing = newBearing;
            hasValidBearing = true;
        } else {
            // Làm mượt bearing để tránh giật lag
            float bearingDiff = Math.abs(newBearing - currentBearing);

            // Xử lý trường hợp chênh lệch qua 0/360 độ
            if (bearingDiff > 180) {
                bearingDiff = 360 - bearingDiff;
            }

            // Chỉ cập nhật nếu thay đổi đáng kể (tránh rung lắc)
            if (bearingDiff > BEARING_THRESHOLD) {
                // Làm mượt bearing với trọng số
                currentBearing = interpolateBearing(currentBearing, newBearing, 0.3f);
            }
        }
    }

    // THÊM PHƯƠNG THỨC NỘI SUY BEARING
    private float interpolateBearing(float currentBearing, float targetBearing, float factor) {
        float diff = targetBearing - currentBearing;

        // Xử lý góc qua 0/360
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }

        float result = currentBearing + (diff * factor);

        // Đảm bảo kết quả trong khoảng 0-360
        if (result < 0) {
            result += 360;
        } else if (result >= 360) {
            result -= 360;
        }

        return result;
    }

    // THAY THẾ PHƯƠNG THỨC calculateBearing CŨ
    private float calculateBearing(LatLng from, LatLng to) {
        if (from == null || to == null) return -1;

        double lat1 = Math.toRadians(from.latitude);
        double lat2 = Math.toRadians(to.latitude);
        double deltaLng = Math.toRadians(to.longitude - from.longitude);

        double x = Math.sin(deltaLng) * Math.cos(lat2);
        double y = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLng);

        double bearing = Math.toDegrees(Math.atan2(x, y));
        float result = (float) ((bearing + 360) % 360);



        return result;
    }

    // CẢI TIẾN PHƯƠNG THỨC updateCameraPosition
    private void updateCameraPosition() {
        if (mMap == null || currentUserLocation == null) return;

        // Hủy callback cũ
        if (cameraUpdateRunnable != null) {
            cameraUpdateHandler.removeCallbacks(cameraUpdateRunnable);
        }

        cameraUpdateRunnable = () -> {
            if (mMap != null && currentUserLocation != null && (isNavigating || isRouteMode)) {

                // QUAN TRỌNG: Chỉ cập nhật camera khi không có LocationManager đang tracking
                // Tránh xung đột giữa 2 hệ thống cập nhật camera
                if (locationManager != null && locationManager.isCameraTracking()) {
                    return; // Để LocationManager xử lý camera tracking
                }

                // SỬ DỤNG BEARING TỪ GPS CHỈ KHI CÓ BEARING HỢP LỆ
                float bearingToUse = 0f;

                if (hasValidBearing && shouldFollowBearing) {
                    bearingToUse = currentBearing;
                } else if (isRouteMode) {
                    // Trong chế độ route mode, ưu tiên giữ bearing hiện tại của camera
                    // thay vì reset về 0
                    CameraPosition currentCameraPosition = mMap.getCameraPosition();
                    bearingToUse = currentCameraPosition.bearing;
                }

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(currentUserLocation)
                        .zoom(TRACKING_ZOOM_LEVEL)
                        .bearing(bearingToUse)
                        .tilt(shouldFollowBearing ? TILT_ANGLE : 0f)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 800, null);
            }
        };

        // Giảm delay để phản hồi nhanh hơn
        cameraUpdateHandler.postDelayed(cameraUpdateRunnable, 200);
    }


    // Cập nhật vị trí marker người dùng trên bản đồ
    private void updateUserMarkerPosition() {
        if (mMap != null && currentUserLocation != null) {
            locationManager.updateUserMarkerPosition(currentUserLocation);

            // CHỈ ĐIỀU KHIỂN CAMERA KHI KHÔNG PHẢI ROUTE MODE HOẶC KHI KHÔNG THEO DÕI
            if (!shouldTrackLocation || (!isNavigating && !isRouteMode)) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentUserLocation));
            }
            // Nếu đang trong route mode, để LocationManager xử lý camera
        }
    }

    // Callback khi bản đồ đã sẵn sàng để thao tác
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Thiết lập các cài đặt UI của bản đồ: bật tắt các thao tác zoom, cuộn, nghiêng, xoay, la bàn
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true); // Bật nút định vị mặc định
        mMap.getUiSettings().setMapToolbarEnabled(true);

        // Giới hạn mức zoom bản đồ
        mMap.setMinZoomPreference(6.0f);
        mMap.setMaxZoomPreference(20.0f);

        // Thiết lập giao diện custom cho cửa sổ info marker
//        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);

        // Khi nhấn marker thì hiện info window
        mMap.setOnMarkerClickListener(marker -> {
            locationManager.onInfoWindowClick(marker);
//            locationManager.onInfoWindowClick(marker); // Gọi dialog "Đóng / Dẫn đường"
            return true;
        });

        // Khởi tạo locationManager quản lý vị trí, marker, điều hướng
        locationManager = new LocationManager(requireContext(), mMap);

        // Thiết lập callback sự kiện dẫn đường từ locationManager
        locationManager.setNavigationCallback(new LocationManager.NavigationCallback() {
            @Override
            public void onNavigationStarted(Marker destination) {
                isNavigating = true;
                destinationMarker = destination;
                shouldTrackLocation = true;
                shouldFollowBearing = true; // BẬT THEO DÕI HƯỚNG GPS
                hasValidBearing = false; // Reset bearing
                Toast.makeText(requireContext(), "Bắt đầu dẫn đường với GPS bearing", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNavigationEnded() {
                isNavigating = false;
                isRouteMode = false;
                destinationMarker = null;
                shouldTrackLocation = false;
                shouldFollowBearing = false;
                hasValidBearing = false; // Reset bearing
                resetCameraView();
                Toast.makeText(requireContext(), "Đã kết thúc dẫn đường", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNavigationUpdated() {
                // Xử lý khi cập nhật dẫn đường (có thể dùng để vẽ tuyến đường mới)
            }

            @Override
            public void onRouteCalculated() {
                // Khi tính được tuyến đường ngắn nhất
                isRouteMode = true;
                shouldTrackLocation = true;
                shouldFollowBearing = true;
                hasValidBearing = false; // Reset bearing
                Toast.makeText(requireContext(), "Chế độ theo dõi lộ trình với GPS bearing", Toast.LENGTH_SHORT).show();
            }
        });

        // Kiểm tra quyền truy cập vị trí, nếu có thì bật định vị trên bản đồ
        if (checkLocationPermission()) {
            enableMyLocation();
        } else {
            requestLocationPermission();
        }

        // Thêm marker mặc định: trường HCMUT
        LatLng hcmutLocation = new LatLng(10.762622, 106.660172);
        mMap.addMarker(new MarkerOptions()
                .position(hcmutLocation)
                .title("HCMUT")
                .snippet("Ho Chi Minh University of Technology"));

        // Di chuyển camera đến vị trí mặc định và zoom 15
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmutLocation, 15));

        // Khi người dùng nhấn vào bản đồ (chỗ không có marker)
        mMap.setOnMapClickListener(latLng -> {
            if (isNavigating) {
                stopNavigation(); // Dừng dẫn đường nếu đang đi
            }
            if (isRouteMode) {
                shouldTrackLocation = false; // Tạm dừng theo dõi lộ trình
                Toast.makeText(requireContext(), "Chế độ theo dõi tạm dừng. Nhấn vào GPS để tiếp tục.", Toast.LENGTH_SHORT).show();
            }
        });

        // Khi camera bị di chuyển thủ công bởi người dùng (bằng thao tác tay)
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                shouldTrackLocation = false; // Tắt chế độ theo dõi vị trí tự động
            }
        });

        // Lấy dữ liệu các location từ API, sau đó thêm marker lên bản đồ
        locationManager.fetchAllLocations(locations -> addMarkersForLocations(locations));
    }



    // THÊM PHƯƠNG THỨC KIỂM TRA VÀ HIỆU CHỈNH LA BÀN
    private void calibrateCompass() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Hiệu chỉnh la bàn")
                .setMessage("Để la bàn hoạt động chính xác, hãy xoay điện thoại theo hình số 8 trong không khí vài lần.")
                .setPositiveButton("Đã hiểu", null)
                .show();
    }



    // Đưa camera về vị trí hiện tại, zoom 16, góc xoay và nghiêng về 0
    private void resetCameraView() {
        if (mMap != null && currentUserLocation != null) {
            // Reset bearing flag
            hasValidBearing = false;
            shouldFollowBearing = false;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentUserLocation)
                    .zoom(16f)
                    .bearing(0)  // Reset về hướng Bắc
                    .tilt(0)     // Không nghiêng
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500, null);
        }
    }

    // THÊM PHƯƠNG THỨC ĐỂ BẬT/TẮT CHẾ ĐỘ THEO DÕI HƯỚNG
    public void toggleBearingMode() {
        shouldFollowBearing = !shouldFollowBearing;

        if (shouldFollowBearing && !hasValidBearing) {
            Toast.makeText(requireContext(), "Đang chờ tín hiệu GPS để xác định hướng...", Toast.LENGTH_SHORT).show();
        }

        String message = shouldFollowBearing ? "Bật chế độ theo dõi hướng GPS" : "Tắt chế độ theo dõi hướng GPS";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // Cập nhật camera ngay lập tức
        if (shouldTrackLocation && (isNavigating || isRouteMode)) {
            updateCameraPosition();
        }
    }



    // THÊM PHƯƠNG THỨC TÍNH KHOẢNG CÁCH
    private float calculateDistance(LatLng from, LatLng to) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                from.latitude, from.longitude,
                to.latitude, to.longitude,
                results
        );
        return results[0];
    }



    // Thêm marker lên bản đồ từ danh sách locations lấy từ API, lưu map để dùng sau
    private void addMarkersForLocations(List<com.android.projectandroid.model.Location> locations) {
        markerLocationMap.clear();

        for (com.android.projectandroid.model.Location location : locations) {
            LatLng position = new LatLng(
                    location.getPosition().getLat(),
                    location.getPosition().getLng()
            );

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .snippet(location.getAddress()));

            if (marker != null) {
                markerLocationMap.put(marker, location);
            }
        }
    }

    // Khi người dùng nhấn vào cửa sổ info của marker
    @Override
    public void onInfoWindowClick(Marker marker) {
        locationManager.onInfoWindowClick(marker);
    }

    // Adapter hiển thị cửa sổ info window tùy chỉnh cho marker

    // Bắt đầu chế độ dẫn đường đến marker đích
    private void startNavigation(Marker marker) {
        destinationMarker = marker;
        isNavigating = true;
        shouldTrackLocation = true;
        shouldFollowBearing = true; // Bật theo dõi hướng GPS

        // Reset bearing để bắt đầu mới
        hasValidBearing = false;
        currentBearing = 0f;

        // Bắt đầu lấy vị trí GPS
        startLocationUpdates();

        // Khởi động dẫn đường
        locationManager.startNavigation(marker);

        Toast.makeText(requireContext(), "Bắt đầu dẫn đường với GPS bearing", Toast.LENGTH_SHORT).show();
    }

    // Dừng dẫn đường
    private void stopNavigation() {
        isNavigating = false;
        isRouteMode = false;
        destinationMarker = null;
        shouldTrackLocation = false;
        shouldFollowBearing = false;

        // Dừng cập nhật vị trí GPS
        stopLocationUpdates();

        // Hủy các Runnable cập nhật navigation nếu có
        if (navigationRunnable != null) {
            navigationHandler.removeCallbacks(navigationRunnable);
        }

        // Reset camera về vị trí mặc định
        resetCameraView();

        // Dừng dẫn đường trong LocationManager
        locationManager.stopNavigation();

        Toast.makeText(requireContext(), "Đã dừng dẫn đường", Toast.LENGTH_SHORT).show();
    }

    // Cập nhật tuyến đường khi đang dẫn đường (được quản lý bởi LocationManager)
    private void updateNavigationRoute() {
        if (!isNavigating || destinationMarker == null || currentUserLocation == null) {
            return;
        }
        // Logic update route được xử lý trong LocationManager
    }

    // Bắt đầu cập nhật vị trí từ GPS nếu có quyền
    private void startLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(highAccuracyLocationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
    }

    // Dừng cập nhật vị trí GPS
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // Kiểm tra quyền truy cập vị trí
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Yêu cầu quyền truy cập vị trí từ người dùng
    private void requestLocationPermission() {
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, LOCATION_PERMISSION_REQUEST_CODE);
    }

    // Bật chế độ hiển thị vị trí trên bản đồ và bắt đầu lấy vị trí hiện tại
    private void enableMyLocation() {
        if (mMap != null && checkLocationPermission()) {
            try {
                mMap.setMyLocationEnabled(true);
                getCurrentLocation(); // Lấy vị trí cuối cùng đã lưu
                startLocationUpdates(); // Bắt đầu nhận vị trí mới
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Lỗi khi kích hoạt định vị: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Lấy vị trí cuối cùng (last known location) và cập nhật marker người dùng
    private void getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<android.location.Location>() {
                        @Override
                        public void onSuccess(android.location.Location location) {
                            if (location != null && mMap != null) {
                                currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                if (locationManager != null) {
                                    locationManager.updateCurrentLocation(currentUserLocation);
                                }

                                updateUserMarkerPosition();
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 16));
                            } else {
                                Toast.makeText(requireContext(), "Đang chờ tín hiệu GPS...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // Xử lý kết quả trả về khi người dùng đồng ý hoặc từ chối cấp quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation(); // Đã cấp quyền thì bật định vị
            } else {
                Toast.makeText(requireContext(), "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Khi bấm nút filter trên giao diện
    private void onFilterButtonClick() {
        showFilterOptionsDialog();
    }

    // Hiển thị dialog tùy chọn lọc danh sách điểm trên bản đồ
    private void showFilterOptionsDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.filter_options_dialog);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        ImageButton btnClose = dialog.findViewById(R.id.btn_dialog_close);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        LinearLayout optionShowAll = dialog.findViewById(R.id.option_show_all);
        optionShowAll.setOnClickListener(v -> {
            locationManager.filterByRegion(null); // Hiển thị tất cả điểm
            dialog.dismiss();
        });

        LinearLayout optionFilterRegion = dialog.findViewById(R.id.option_filter_region);
        optionFilterRegion.setOnClickListener(v -> {
            locationManager.toggleRegionFilter(); // Bật/tắt lọc theo vùng
            dialog.dismiss();
        });

        LinearLayout optionShortestRoute = dialog.findViewById(R.id.option_shortest_route);
        optionShortestRoute.setOnClickListener(v -> {
            if (currentUserLocation == null) {
                Toast.makeText(requireContext(), "Vị trí hiện tại không khả dụng", Toast.LENGTH_SHORT).show();
                return;
            }
            locationManager.findShortestRoute(currentUserLocation); // Tính lộ trình ngắn nhất
            dialog.dismiss();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
    }

    // Zoom bản đồ lớn hơn
    public void zoomIn() {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
    }

    // Zoom bản đồ nhỏ hơn
    public void zoomOut() {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }

    // Khi fragment tạm dừng (chuyển tab, app về background...)
    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        if (isNavigating) {
            stopNavigation();
        }
    }

    // Khi fragment tiếp tục hoạt động
    @Override
    public void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            startLocationUpdates();
            if (shouldFollowBearing) {
            }
            if (isNavigating && destinationMarker != null) {
                updateNavigationRoute();
            }
        }
    }

    // Khi fragment bị hủy hoàn toàn
    @Override
    public void onDestroy() {
        super.onDestroy();
//        disableSensors();
        if (navigationHandler != null && navigationRunnable != null) {
            navigationHandler.removeCallbacks(navigationRunnable);
        }
        if (cameraUpdateHandler != null && cameraUpdateRunnable != null) {
            cameraUpdateHandler.removeCallbacks(cameraUpdateRunnable);
        }
    }
}
