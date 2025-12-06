# Hướng dẫn sử dụng Retrofit với NestJS Backend

## 📚 Tổng quan

Hướng dẫn này giải thích cách triển khai Retrofit trong Android app để kết nối với NestJS backend, bao gồm:
- Lưu trữ và quản lý access_token
- Tự động thêm Authorization header
- Xử lý token hết hạn và redirect về login
- Cấu trúc thư mục và code organization

---

## 🗂️ Cấu trúc thư mục

```
app/src/main/java/com/example/cloudstorage/
├── api/
│   ├── ApiClient.java           # Retrofit singleton, cấu hình OkHttpClient
│   ├── ApiService.java          # Interface định nghĩa API endpoints
│   └── AuthInterceptor.java     # Tự động thêm token + xử lý 401
├── models/
│   ├── LoginRequest.java        # Request model cho login
│   ├── LoginResponse.java       # Response model từ login API
│   └── User.java                # User model
├── utils/
│   └── TokenManager.java        # Quản lý lưu/đọc/xóa token
├── activities/
│   ├── MainActivity.java        # Login screen
│   ├── HomePage.java            # Home screen
│   └── ...
└── ...
```

---

## ❓ Câu trả lời cho các câu hỏi của bạn

### 1. **Lưu access_token ở đâu trong mobile app?**

**Trả lời:** Sử dụng **SharedPreferences** (Android) hoặc **UserDefaults** (iOS)

**Lý do:**
- ✅ Dữ liệu được lưu vĩnh viễn trong bộ nhớ internal storage
- ✅ Tồn tại ngay cả khi user thoát app hoặc khởi động lại thiết bị
- ✅ Chỉ bị xóa khi uninstall app hoặc clear app data
- ✅ Bảo mật ở mức app-level (MODE_PRIVATE)

**Implementation:**
```java
// Lưu token
TokenManager tokenManager = new TokenManager(context);
tokenManager.saveToken(accessToken);

// Đọc token
String token = tokenManager.getToken();

// Xóa token (logout hoặc hết hạn)
tokenManager.clearToken();
```

**File:** [TokenManager.java](app/src/main/java/com/example/cloudstorage/utils/TokenManager.java)

---

### 2. **Làm sao thêm token vào Authorization header mỗi lần gọi API?**

**Trả lời:** Sử dụng **OkHttp Interceptor**

**Lợi ích:**
- ✅ Tự động thêm token vào TẤT CẢ request
- ✅ Không cần thêm header manually ở mỗi API call
- ✅ Centralized logic - dễ maintain

**Implementation:**
```java
public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Lấy token từ SharedPreferences
        String token = tokenManager.getToken();

        // Thêm vào header
        Request request = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer " + token)
            .build();

        return chain.proceed(request);
    }
}
```

**File:** [AuthInterceptor.java](app/src/main/java/com/example/cloudstorage/api/AuthInterceptor.java)

---

### 3. **Token có bị mất khi user thoát app không?**

**Trả lời:** **KHÔNG**, token được lưu trong SharedPreferences sẽ KHÔNG bị mất

**Các trường hợp token VẪN TỒN TẠI:**
- ✅ User thoát app (kill app)
- ✅ User khởi động lại thiết bị
- ✅ App bị crash
- ✅ Ngắt nguồn điện thoại

**Các trường hợp token BỊ XÓA:**
- ❌ User uninstall app
- ❌ User clear app data trong Settings
- ❌ App gọi `tokenManager.clearToken()` (logout)
- ❌ Token hết hạn và AuthInterceptor xử lý 401

**Best Practice:**
- Nếu token expire time NGẮN (vài giờ): Lưu trong SharedPreferences là đủ
- Nếu token expire time DÀI (nhiều ngày): Vẫn lưu SharedPreferences, nhưng nên implement refresh token
- Có thể thêm encryption nếu cần bảo mật cao hơn

---

### 4. **Có cần check và redirect về login như web không?**

**Trả lời:** **CÓ**, nhưng khác với web một chút

**Các trường hợp cần redirect về login:**

#### a) Khi mở app (onCreate)
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    TokenManager tokenManager = new TokenManager(this);

    // Check nếu chưa login
    if (!tokenManager.isLoggedIn()) {
        navigateToLogin();
        return;
    }

    // Nếu đã login, tiếp tục load data
    loadUserProfile();
}
```

#### b) Khi token hết hạn (401 Unauthorized)
**AuthInterceptor tự động xử lý:**
```java
@Override
public Response intercept(Chain chain) throws IOException {
    Response response = chain.proceed(request);

    // Kiểm tra response code
    if (response.code() == 401) {
        handleUnauthorized(); // Tự động clear token + redirect
    }

    return response;
}

private void handleUnauthorized() {
    // 1. Xóa token
    tokenManager.clearToken();

    // 2. Redirect về login
    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    context.startActivity(intent);

    // 3. Hiển thị thông báo
    Toast.makeText(context, "Phiên đăng nhập hết hạn", Toast.LENGTH_LONG).show();
}
```

**File:** [AuthInterceptor.java](app/src/main/java/com/example/cloudstorage/api/AuthInterceptor.java)

---

## 🚀 Cách sử dụng

### 1. Cấu hình Backend URL

Mở [ApiClient.java](app/src/main/java/com/example/cloudstorage/api/ApiClient.java) và đổi BASE_URL:

```java
// Android Emulator (localhost)
private static final String BASE_URL = "http://10.0.2.2:3000/api/";

// Real device (cùng WiFi với máy dev)
// private static final String BASE_URL = "http://192.168.1.100:3000/api/";

// Production
// private static final String BASE_URL = "https://your-backend.com/api/";
```

**Lưu ý:**
- `10.0.2.2` là địa chỉ localhost của host machine khi chạy trên Android Emulator
- `192.168.x.x` là IP local của máy dev (chạy NestJS) khi test trên real device
- Phải bật `android:usesCleartextTraffic="true"` để dùng HTTP (đã config trong AndroidManifest.xml)

---

### 2. Đăng nhập (Login)

```java
// 1. Tạo request
LoginRequest loginRequest = new LoginRequest(email, password);

// 2. Gọi API
ApiClient.getApiService(this).login(loginRequest).enqueue(new Callback<LoginResponse>() {
    @Override
    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            String accessToken = response.body().getAccessToken();

            // 3. Lưu token
            TokenManager tokenManager = new TokenManager(MainActivity.this);
            tokenManager.saveToken(accessToken);

            // 4. Chuyển đến HomePage
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onFailure(Call<LoginResponse> call, Throwable t) {
        Toast.makeText(MainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }
});
```

**File ví dụ:** [MainActivityExample.java](app/src/main/java/com/example/cloudstorage/MainActivityExample.java)

---

### 3. Gọi API cần Authorization

```java
// Authorization header tự động được thêm bởi AuthInterceptor
ApiClient.getApiService(this).getProfile().enqueue(new Callback<User>() {
    @Override
    public void onResponse(Call<User> call, Response<User> response) {
        if (response.isSuccessful() && response.body() != null) {
            User user = response.body();
            // Hiển thị thông tin user
        }
    }

    @Override
    public void onFailure(Call<User> call, Throwable t) {
        Toast.makeText(this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }
});
```

**Lưu ý:** Không cần thêm `.addHeader("Authorization", ...)` manually!

**File ví dụ:** [HomePageExample.java](app/src/main/java/com/example/cloudstorage/HomePageExample.java)

---

### 4. Logout

```java
private void handleLogout() {
    // Xóa token
    TokenManager tokenManager = new TokenManager(this);
    tokenManager.clearToken();

    // Chuyển về login
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

---

### 5. Kiểm tra login khi mở app

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    TokenManager tokenManager = new TokenManager(this);

    // Nếu đã login, chuyển thẳng đến HomePage
    if (tokenManager.isLoggedIn()) {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
        finish();
        return;
    }

    // Nếu chưa login, hiển thị màn hình login
    setContentView(R.layout.welcome_page);
    // ... setup login form
}
```

---

## 🔧 API Endpoints

Thêm endpoint mới vào [ApiService.java](app/src/main/java/com/example/cloudstorage/api/ApiService.java):

```java
public interface ApiService {
    // Existing endpoints
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("profile")
    Call<User> getProfile();

    // Thêm endpoint mới
    @GET("files")
    Call<List<File>> getFiles();

    @POST("files/upload")
    Call<File> uploadFile(@Body RequestBody file);

    @DELETE("files/{id}")
    Call<Void> deleteFile(@Path("id") String fileId);

    @PUT("profile")
    Call<User> updateProfile(@Body User user);
}
```

---

## 🐛 Debug

Xem request/response trong Logcat:

```
# Filter: "OkHttp"
--> POST http://10.0.2.2:3000/api/login
Content-Type: application/json
{"email":"user@example.com","password":"123456"}
--> END POST

<-- 200 OK http://10.0.2.2:3000/api/login
Content-Type: application/json
{"access_token":"eyJhbGc...","message":"Login successful"}
<-- END HTTP
```

**Logging được cấu hình trong:** [ApiClient.java](app/src/main/java/com/example/cloudstorage/api/ApiClient.java)

---

## ✅ Checklist triển khai

- [x] Thêm dependencies vào `build.gradle.kts`
- [x] Thêm Internet permission vào `AndroidManifest.xml`
- [x] Tạo Model classes (LoginRequest, LoginResponse, User)
- [x] Tạo TokenManager để lưu/đọc token
- [x] Tạo AuthInterceptor để thêm token + xử lý 401
- [x] Tạo ApiService interface
- [x] Tạo ApiClient singleton
- [x] Implement login trong MainActivity
- [x] Kiểm tra isLoggedIn() khi mở app
- [x] Gọi API /profile trong HomePage
- [x] Implement logout
- [ ] Test token hết hạn (401 response)
- [ ] Đổi BASE_URL thành backend URL thực tế
- [ ] Test trên real device

---

## 📝 Lưu ý quan trọng

### 1. Android Emulator vs Real Device

**Android Emulator:**
```java
private static final String BASE_URL = "http://10.0.2.2:3000/api/";
```

**Real Device (cùng WiFi):**
```bash
# Trên terminal, kiểm tra IP của máy dev:
ifconfig | grep "inet "  # macOS/Linux
ipconfig                  # Windows

# Sử dụng IP đó:
private static final String BASE_URL = "http://192.168.1.100:3000/api/";
```

### 2. HTTPS vs HTTP

- Development: Dùng HTTP với `android:usesCleartextTraffic="true"`
- Production: PHẢI dùng HTTPS, xóa `usesCleartextTraffic`

### 3. Token Expiration

- Nếu không dùng refresh token, nên set expire time hợp lý (ví dụ: 7 ngày)
- AuthInterceptor sẽ tự động redirect về login khi nhận 401
- Có thể hiển thị dialog "Token hết hạn" trước khi redirect

### 4. Error Handling

Luôn xử lý cả `onResponse` (network success) và `onFailure` (network error):

```java
@Override
public void onResponse(Call<T> call, Response<T> response) {
    if (response.isSuccessful()) {
        // 200, 201, etc.
    } else {
        // 400, 401, 404, 500, etc.
        // Parse error message từ response.errorBody()
    }
}

@Override
public void onFailure(Call<T> call, Throwable t) {
    // Network error, timeout, etc.
}
```

---

## 🎯 Next Steps

1. Copy logic từ `MainActivityExample.java` vào `MainActivity.java`
2. Copy logic từ `HomePageExample.java` vào `HomePage.java`
3. Test login flow end-to-end
4. Implement các API endpoints khác (upload file, get files, etc.)
5. Thêm error handling và loading states
6. Implement refresh token (optional, nếu cần)

---

## 📚 Tài liệu tham khảo

- [Retrofit Documentation](https://square.github.io/retrofit/)
- [OkHttp Interceptors](https://square.github.io/okhttp/interceptors/)
- [Android SharedPreferences](https://developer.android.com/training/data-storage/shared-preferences)
- [Android Network Security Config](https://developer.android.com/training/articles/security-config)

---

**Happy Coding! 🚀**
