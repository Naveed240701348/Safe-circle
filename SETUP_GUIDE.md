# SafeCircle - Quick Setup Guide

## 🚀 **Steps to Run SafeCircle App**

### **1. Firebase Setup (Required)**

#### Create Firebase Project:
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" → Name it "SafeCircle"
3. Continue with default settings
4. Click "Create project"

#### Add Android App:
1. In Firebase project, click "Add app" → Android icon
2. Package name: `com.safecircle`
3. App nickname: `SafeCircle`
4. Click "Register app"
5. Download `google-services.json`
6. **Replace the placeholder file** in your project:
   - Move `google-services.json` to: `app/src/main/google-services.json`

#### Enable Firebase Services:
1. **Authentication**:
   - Go to "Authentication" → "Get started"
   - Enable "Email/Password" provider
   - Click "Save"

2. **Firestore Database**:
   - Go to "Firestore Database" → "Create database"
   - Choose "Start in test mode" (for development)
   - Select a location (choose nearest to you)
   - Click "Create"

3. **Cloud Messaging**:
   - Go to "Cloud Messaging" → "Get started"
   - It's automatically enabled

### **2. Android Studio Setup**

#### System Requirements:
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK API 24+
- Java 8+

#### Import Project:
1. Open Android Studio
2. Click "Open" → Navigate to your SafeCircle folder
3. Wait for Gradle sync (may take 2-5 minutes)

### **3. Fix Build Issues (if any)**

#### If Gradle sync fails:
1. Open `build.gradle` (project level)
2. Update plugin versions if needed:
   ```gradle
   id 'com.android.application' version '8.2.0' apply false
   id 'org.jetbrains.kotlin.android' version '1.9.20' apply false
   id 'com.google.gms.google-services' version '4.4.0' apply false
   ```

#### If dependencies fail:
1. Open `app/build.gradle`
2. Update Firebase BOM:
   ```gradle
   implementation platform('com.google.firebase:firebase-bom:32.7.0')
   ```

### **4. Create Emulator or Connect Device**

#### Android Emulator:
1. In Android Studio: Tools → Device Manager
2. Click "Create device"
3. Choose phone (Pixel 6 recommended)
4. Select system image (API 30+ with Google Play Services)
5. Finish setup → Launch emulator

#### Physical Device:
1. Enable Developer Options on your phone
2. Enable USB Debugging
3. Connect via USB
4. Allow debugging when prompted

### **5. Run the App**

#### From Android Studio:
1. Select your device/emulator from dropdown
2. Click green "Run" button (▶️)
3. Wait for build and installation

#### Check for Issues:
- **Build successful**: App will install and launch
- **Build failed**: Check error messages in Build tab

### **6. Test the App**

#### First Run:
1. **Register** a new user account
2. **Login** with your credentials
3. **Add friends** (need multiple test accounts):
   - Register second user on another device/emulator
   - Search by email and send friend request
   - Accept request on second device

#### Test SOS Feature:
1. Grant location permissions when prompted
2. Press the red SOS button
3. Check if notification appears on friend's device

### **7. Common Issues & Solutions**

#### "google-services.json missing":
- Ensure file is in `app/src/main/` folder
- Check package name matches Firebase project

#### "Location permission denied":
- Go to Settings → Apps → SafeCircle → Permissions
- Enable Location permission

#### "Notifications not working":
- Enable notification permissions (Android 13+)
- Check if app is blocked from notifications

#### "Firebase connection failed":
- Check internet connection
- Verify Firebase project setup
- Re-download google-services.json

#### "Build errors":
- File → Invalidate Caches → Restart
- Clean project: Build → Clean Project
- Rebuild project: Build → Rebuild Project

### **8. Development Tips**

#### Debugging:
- Use Logcat to view app logs
- Add breakpoints in ViewModels
- Check Firebase Console for data

#### Testing:
- Test with multiple devices/emulators
- Test SOS with different locations
- Verify friend request flow

#### Performance:
- Monitor memory usage in Android Studio
- Check network requests in Network Inspector

---

## 🎯 **Quick Start Checklist**

- [ ] Firebase project created
- [ ] `google-services.json` downloaded and placed correctly
- [ ] Authentication enabled (Email/Password)
- [ ] Firestore database created
- [ ] Cloud Messaging enabled
- [ ] Android Studio project imported
- [ ] Gradle sync successful
- [ ] Emulator/device ready
- [ ] App builds and runs
- [ ] User registration works
- [ ] Friend system works
- [ ] SOS button works

**Estimated Setup Time**: 15-30 minutes
