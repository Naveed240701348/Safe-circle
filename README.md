# SafeCircle - Personal Safety App

SafeCircle is a personal safety Android application where users can connect with friends and family. When a user presses an SOS button, an emergency alert with their live location is sent to all friends in their friend list.

## Features

- **Firebase Authentication**: Email/Password login and registration
- **Friend System**: Search users by email, send and accept friend requests
- **SOS Emergency Alert**: One-tap emergency button with live GPS location
- **Real-time Notifications**: Firebase Cloud Messaging for instant alerts
- **Modern UI**: Built with Jetpack Compose and Material Design 3
- **Location Services**: GPS integration for accurate location sharing

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: Firebase (Authentication, Firestore, Cloud Messaging)
- **Location**: Google Play Services Location API
- **Navigation**: Compose Navigation
- **Async**: Coroutines

## Project Structure

```
app/
├── src/main/java/com/safecircle/
│   ├── data/model/           # Data models (User, Friend, etc.)
│   ├── repository/           # Repository pattern implementation
│   ├── service/              # Services (Location, FCM)
│   ├── ui/
│   │   ├── screen/           # Compose screens
│   │   ├── viewmodel/        # ViewModels
│   │   ├── navigation/       # Navigation setup
│   │   └── theme/            # UI theme
│   └── MainActivity.kt       # Main activity
└── src/main/res/             # Android resources
```

## Setup Instructions

### 1. Prerequisites

- Android Studio Hedgehog | 2023.1.1 or later
- Android SDK API 24 (Android 7.0) or higher
- Firebase account

### 2. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project named "SafeCircle"
3. Add an Android app with package name `com.safecircle`
4. Download `google-services.json` and place it in `app/src/main/`
5. Enable the following Firebase services:
   - **Authentication**: Email/Password provider
   - **Firestore Database**: Create database in test mode
   - **Cloud Messaging**: Enable for notifications

### 3. Firestore Database Setup

Create the following collections and indexes:

**Users Collection**:
- Collection: `users`
- Fields: `userId`, `name`, `email`, `fcmToken`, `createdAt`

**Friends Collection**:
- Collection: `friends`
- Fields: `userId`, `friendId`, `addedAt`

**Friend Requests Collection**:
- Collection: `friendRequests`
- Fields: `requestId`, `fromUserId`, `toUserId`, `fromUserName`, `fromUserEmail`, `createdAt`

### 4. Build Configuration

The project uses Gradle with the following key configurations:

- **compileSdk**: 34
- **minSdk**: 24
- **targetSdk**: 34
- **Kotlin**: 1.9.10
- **Compose BOM**: 2023.10.01

### 5. Running the App

1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Create an Android Virtual Device (AVD) or connect a physical device
4. Ensure the device has Google Play Services
5. Run the app

## Permissions Required

The app requires the following permissions:

- `INTERNET`: Network access for Firebase
- `ACCESS_FINE_LOCATION`: GPS location for SOS
- `ACCESS_COARSE_LOCATION`: Approximate location
- `POST_NOTIFICATIONS`: Push notifications (Android 13+)

## Usage Guide

### Registration & Login

1. Open the app and click "Register"
2. Enter your name, email, and password
3. Verify your email if required
4. Login with your credentials

### Adding Friends

1. From the SOS screen, tap the friends icon
2. Tap "Add Friend" button
3. Enter your friend's email address
4. Send friend request
5. Your friend will receive a notification to accept

### Using SOS Feature

1. From the main SOS screen, press the large red SOS button
2. Grant location permissions if prompted
3. The app will:
   - Get your current GPS location
   - Generate a Google Maps link
   - Send emergency alert to all friends
   - Show confirmation when sent

### Receiving SOS Alerts

When a friend sends an SOS alert:
- You'll receive a push notification
- The notification includes your friend's name and location
- Tap the notification to open Google Maps

## Database Schema

### User Document
```json
{
  "userId": "string",
  "name": "string",
  "email": "string",
  "fcmToken": "string",
  "createdAt": "timestamp"
}
```

### Friend Document
```json
{
  "userId": "string",
  "friendId": "string",
  "addedAt": "timestamp"
}
```

### Friend Request Document
```json
{
  "requestId": "string",
  "fromUserId": "string",
  "toUserId": "string",
  "fromUserName": "string",
  "fromUserEmail": "string",
  "createdAt": "timestamp"
}
```

## Troubleshooting

### Build Issues

1. **Gradle Sync Failures**: 
   - Check internet connection
   - Update Android Studio
   - Clear Gradle cache (`File > Invalidate Caches`)

2. **Firebase Connection Issues**:
   - Verify `google-services.json` is in correct location
   - Check Firebase console for project setup
   - Ensure package name matches Firebase project

### Runtime Issues

1. **Location Not Working**:
   - Enable GPS on device
   - Grant location permissions
   - Check if Google Play Services is updated

2. **Notifications Not Working**:
   - Grant notification permissions (Android 13+)
   - Check if app is in battery optimization whitelist
   - Verify FCM setup in Firebase console

### Common Errors

- **"Location permissions not granted"**: Grant permissions in device settings
- **"User not found"**: Verify email address is correct and user is registered
- **"Failed to send SOS"**: Check internet connection and location services

## Future Enhancements

- Emergency contact integration
- Voice-activated SOS
- Location history tracking
- Group emergency alerts
- Integration with emergency services
- Offline mode support
- Multi-language support

## License

This project is for educational purposes. Please ensure compliance with local regulations when implementing emergency alert systems.

## Support

For issues and questions, please check the troubleshooting section or create an issue in the project repository.
