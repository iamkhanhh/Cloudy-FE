# Tóm tắt triển khai Retrofit

## ✅ Những gì đã thêm vào project

### 1. Dependencies (build.gradle.kts)
```kotlin
// Retrofit for API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Gson for JSON parsing
implementation("com.google.code.gson:gson:2.10.1")
```

### 2. Permissions (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<application android:usesCleartextTraffic="true">
```

### 3. Files đã tạo

#### 📁 models/
- `LoginRequest.java` - Request body cho login API
- `LoginResponse.java` - Response từ login API (chứa access_token)
- `User.java` - User model

#### 📁 utils/
- `TokenManager.java` - **Quản lý token** (lưu/đọc/xóa token từ SharedPreferences)

#### 📁 api/
- `ApiService.java` - **Interface định nghĩa API endpoints**
- `AuthInterceptor.java` - **Tự động thêm Authorization header + xử lý 401**
- `ApiClient.java` - **Retrofit singleton**

#### 📄 Examples (để tham khảo)
- `MainActivityExample.java` - Ví dụ login
- `HomePageExample.java` - Ví dụ gọi API /profile

---

## 🎯 Câu trả lời trực tiếp

### 1. Token lưu ở đâu?
→ **SharedPreferences** (file XML trong internal storage)
- ✅ Không mất khi thoát app
- ✅ Không mất khi khởi động lại thiết bị
- ❌ Chỉ mất khi uninstall hoặc clear app data

### 2. Làm sao thêm token vào header?
→ **AuthInterceptor** tự động thêm vào MỌI request
```java
requestBuilder.addHeader("Authorization", "Bearer " + token);
```

### 3. Redirect về login như web?
→ **CÓ**, có 2 cách:

**Cách 1:** Check khi mở app
```java
if (!tokenManager.isLoggedIn()) {
    navigateToLogin();
}
```

**Cách 2:** Tự động khi token hết hạn (401)
```java
// AuthInterceptor tự động xử lý
if (response.code() == 401) {
    clearToken();
    redirectToLogin();
}
```

---

## 🚀 Cách sử dụng

### Bước 1: Đổi BASE_URL
Mở `api/ApiClient.java`, đổi dòng 14:
```java
private static final String BASE_URL = "http://10.0.2.2:3000/api/";
```

### Bước 2: Implement login trong MainActivity.java
Copy logic từ `MainActivityExample.java`:
```java
// 1. Tạo request
LoginRequest loginRequest = new LoginRequest(email, password);

// 2. Gọi API
ApiClient.getApiService(this).login(loginRequest).enqueue(callback);

// 3. Lưu token khi thành công
tokenManager.saveToken(accessToken);

// 4. Chuyển đến HomePage
startActivity(new Intent(this, HomePage.class));
```

### Bước 3: Check login khi mở app
Thêm vào `onCreate()` của MainActivity:
```java
TokenManager tokenManager = new TokenManager(this);
if (tokenManager.isLoggedIn()) {
    startActivity(new Intent(this, HomePage.class));
    finish();
    return;
}
```

### Bước 4: Gọi API /profile trong HomePage
```java
ApiClient.getApiService(this).getProfile().enqueue(new Callback<User>() {
    @Override
    public void onResponse(Call<User> call, Response<User> response) {
        if (response.isSuccessful()) {
            User user = response.body();
            // Hiển thị thông tin user
        }
    }
    // ...
});
```

### Bước 5: Implement logout
```java
tokenManager.clearToken();
Intent intent = new Intent(this, MainActivity.class);
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(intent);
```

---

## 🔄 Flow hoàn chỉnh

```
1. User mở app
   └─> TokenManager.isLoggedIn()?
       ├─> YES → Chuyển đến HomePage
       └─> NO  → Hiển thị màn hình Login

2. User nhập email/password → Nhấn Sign In
   └─> ApiClient.login(email, password)
       ├─> Success (200)
       │   └─> Lưu token → Chuyển đến HomePage
       └─> Error (400/401)
           └─> Hiển thị lỗi

3. HomePage gọi API /profile
   └─> AuthInterceptor tự động thêm: "Authorization: Bearer <token>"
       ├─> Success (200)
       │   └─> Hiển thị thông tin user
       └─> Error (401 - Token hết hạn)
           └─> AuthInterceptor tự động:
               1. Xóa token
               2. Redirect về MainActivity
               3. Hiển thị Toast "Phiên đăng nhập hết hạn"

4. User nhấn Logout
   └─> Clear token → Chuyển về MainActivity
```

---

## 📝 Lưu ý quan trọng

### Android Emulator
```java
BASE_URL = "http://10.0.2.2:3000/api/";  // 10.0.2.2 = localhost của host machine
```

### Real Device (cùng WiFi)
```bash
# Check IP máy dev:
ifconfig | grep "inet "

# Dùng IP đó:
BASE_URL = "http://192.168.1.100:3000/api/";
```

### Production
```java
BASE_URL = "https://your-backend.com/api/";
// Và XÓA: android:usesCleartextTraffic="true"
```

---

## 🐛 Debug

Xem request/response trong Logcat:
- Filter: `OkHttp`
- Sẽ thấy tất cả request/response với headers và body

---

## 📚 Đọc thêm

Chi tiết đầy đủ trong: [RETROFIT_GUIDE.md](RETROFIT_GUIDE.md)

---

## ✅ Next Steps

- [ ] Sync Gradle để tải dependencies
- [ ] Đổi BASE_URL thành URL backend thực tế
- [ ] Copy logic từ Example files vào MainActivity và HomePage
- [ ] Test login flow
- [ ] Test token hết hạn (401)
- [ ] Implement các API endpoints khác
