# Final Mobile Application Development
## Group Project Report

---

## Project Title:
**Netflix Clone - Android Streaming Application**

---

## Team Members:

1. **Arhaan** (UI/UX Developer & Frontend Developer)
   - Role: UI Design, Animations, Search Functionality, Profile Management
   - Contribution: Login page UI, shared element transitions, swipe UI, search functionality, profile page, onboarding swipe backend

2. **Naitik** (Backend Developer & Video Player Specialist)
   - Role: Backend Integration, Video Playback, Firebase Integration, Downloads
   - Contribution: ExoPlayer implementation, download functionality, Firebase integration (auth, favorites, continue watching, watch later), Chromecast functionality

3. **Aanchal** (UI/UX Developer & Frontend Developer)
   - Role: UI Design, Animations, User Experience
   - Contribution: Login page UI, shared element transitions, swipe UI, search functionality, profile page (edit/delete profiles, signout)

---

## 1. Introduction

### 1.1 Project Description
This project is a comprehensive Netflix clone Android application that replicates the core functionality of a modern streaming platform. The application provides users with a complete entertainment experience, including user authentication, personalized content recommendations, video playback, offline viewing capabilities, and profile management.

### 1.2 Purpose and Problem Statement
The purpose of this project is to demonstrate proficiency in modern Android development practices while creating a fully functional streaming application. The application addresses the need for:
- **Personalized Content Discovery**: Users can discover movies based on their preferences through an interactive onboarding process
- **Multi-User Support**: Families and individuals can create separate profiles with personalized watch histories and recommendations
- **Offline Viewing**: Users can download content for viewing without internet connectivity
- **Seamless User Experience**: Smooth animations, intuitive navigation, and responsive design enhance user engagement

### 1.3 Target Users
- **Primary Users**: Entertainment enthusiasts who enjoy streaming movies and TV shows
- **Secondary Users**: Families requiring multiple profiles for different members
- **Tertiary Users**: Users who need offline viewing capabilities for travel or limited connectivity scenarios

### 1.4 Overall Vision
The vision is to create a polished, production-ready streaming application that demonstrates:
- Modern Android architecture (MVVM pattern)
- Integration of multiple third-party services (Firebase, TMDB API, ExoPlayer)
- Advanced UI/UX with smooth animations and transitions
- Robust data management and security practices
- Scalable codebase following best practices

---

## 2. Design and UI (15%)

### 2.1 User Interface

#### 2.1.1 Screenshots of Main Screens

**Note**: *[Screenshots should be inserted here for each screen]*

**Main Screens Include:**
1. **Splash Screen**: Animated splash screen with Lottie animation
2. **Get Started Screen**: Onboarding introduction with ViewPager2 showing key features
3. **Login Screen**: Email input screen with Netflix-style design
4. **Password Screen**: Password verification screen
5. **Onboarding Swipe Screen**: Interactive card stack for movie preference selection
6. **Profile Selection Screen**: Grid layout displaying user profiles with smooth transitions
7. **Home Screen (MainActivity)**: 
   - Featured content section ("Only On Netflix")
   - Recommended content section
   - Dynamic genre-based sections
   - Watch List, Continue Watching, Favorites sections
   - "Because You Watched" personalized recommendations
8. **Movie Details Screen**: Detailed movie information with backdrop, title, description
9. **Video Player Screen**: Full-screen landscape player with custom controls
10. **Search Screen**: Grid layout with real-time search and voice recognition
11. **Profile Page**: User profile management with sections for favorites, watch list, continue watching
12. **Downloads Screen**: List of downloaded content for offline viewing
13. **Manage Profiles Screen**: Profile editing and deletion interface

#### 2.1.2 Design Considerations

**Color Scheme:**
- **Primary Background**: Deep black (#000000) for immersive viewing experience
- **Accent Colors**: Netflix red (#E50914) for branding elements
- **Text Colors**: White (#FFFFFF) for primary text, light gray for secondary text
- **Card Backgrounds**: Dark gray (#1A1A1A) for content cards

**Typography:**
- **Headings**: Bold, sans-serif font (Roboto Bold) for section titles
- **Body Text**: Regular Roboto for descriptions and details
- **Font Sizes**: 
  - Section titles: 18sp
  - Movie titles: 16sp
  - Descriptions: 14sp
  - Body text: 12sp

**Icons:**
- Material Design icons for consistency
- Custom icons for profile avatars (color-coded backgrounds)
- Navigation icons in bottom and top navigation bars

**Layouts:**
- **ConstraintLayout**: Primary layout for complex screens
- **LinearLayout**: For vertical content sections
- **RecyclerView**: For horizontal scrolling movie lists
- **GridLayout**: For search results and profile selection
- **CardView**: For movie cards with rounded corners and elevation

**Design Principles:**
- **Dark Theme**: Consistent dark theme throughout for reduced eye strain
- **Edge-to-Edge Display**: Full utilization of screen real estate
- **Material Design**: Following Google's Material Design guidelines
- **Responsive Design**: Adapts to different screen sizes and orientations

#### 2.1.3 User Experience Discussion

**Navigation:**
- **Bottom Navigation Bar**: Quick access to Home and Profile
- **Top Navigation Bar**: Search and Downloads icons for easy access
- **Profile Icon**: Shared element transition for smooth profile switching
- **Back Navigation**: Consistent back button behavior across all screens

**Accessibility:**
- **Touch Targets**: Minimum 48dp touch targets for all interactive elements
- **Content Descriptions**: Proper content descriptions for screen readers
- **Color Contrast**: High contrast ratios for text readability
- **Font Scaling**: Support for system font size preferences

**User Experience Enhancements:**
- **Smooth Animations**: ArcMotion transitions for profile selection
- **Loading States**: Proper loading indicators during data fetching
- **Error Handling**: User-friendly error messages
- **Offline Support**: Clear indicators for downloaded content
- **Immersive Playback**: Full-screen video player with hidden system UI

### 2.2 User Flow

#### 2.2.1 User Flow Diagram

```
[Splash Screen]
    ↓
[Check Authentication]
    ↓
    ├─→ [Not Logged In] → [Get Started] → [Login] → [Password] → [Onboarding Swipe] → [Profile Selection] → [Home]
    │
    └─→ [Logged In] → [Profile Selection] → [Home]
                            ↓
                    [Select Profile]
                            ↓
                    [Home Screen]
                            ↓
        ┌───────────────────┼───────────────────┐
        ↓                   ↓                   ↓
[Search]            [Movie Details]      [Profile Page]
        ↓                   ↓                   ↓
[Search Results]    [Play Video]         [Manage Profiles]
                            ↓                   ↓
                    [Video Player]      [Edit/Delete Profile]
                            ↓
                    [Download/Add to List]
                            ↓
                    [Downloads Screen]
```

#### 2.2.2 Detailed User Flow Description

1. **Initial Launch**: 
   - User opens app → Splash screen with animation
   - System checks authentication status
   - Routes to appropriate screen based on login state

2. **Authentication Flow**:
   - New users: Get Started → Login → Password → Onboarding
   - Returning users: Direct to Profile Selection

3. **Onboarding Flow**:
   - Swipe through movie cards (right = like, left = skip)
   - System generates recommendations based on preferences
   - After 15 swipes or completion → Profile Selection

4. **Profile Selection**:
   - View all profiles in grid layout
   - Select profile with smooth shared element transition
   - Create new profile (up to 5 profiles)

5. **Home Screen Navigation**:
   - Browse content sections
   - Tap movie → Details screen
   - Tap search → Search screen
   - Tap profile icon → Profile page

6. **Content Interaction**:
   - View movie details
   - Play video → Full-screen player
   - Add to favorites/watch list
   - Download for offline viewing

7. **Profile Management**:
   - Access profile page
   - View personal lists (Favorites, Watch List, Continue Watching)
   - Manage profiles (edit, delete)
   - Sign out

---

## 3. Functionality (20%)

### 3.1 Core Features

#### 3.1.1 User Authentication
**How it Works:**
- Firebase Authentication integration
- Email-based login system
- Password verification
- Session persistence across app restarts
- Secure sign-out functionality

**Screenshot Placeholder**: *[Login Screen, Password Screen]*

**Code Implementation:**
```java
// LoginViewModel.java
public void authenticateUser(String email, String password) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                navigateToProfileSelection.postValue(true);
            } else {
                errorMessage.postValue("Authentication failed");
            }
        });
}
```

#### 3.1.2 Multi-Profile Management
**How it Works:**
- Users can create up to 5 profiles per account
- Each profile has customizable name and avatar
- Profile-specific watch history and recommendations
- Smooth shared element transitions when switching profiles
- Profile data stored in Firebase Firestore

**Screenshot Placeholder**: *[Profile Selection Screen, Profile Management Screen]*

**Key Features:**
- Create new profiles with custom avatars
- Edit profile name and avatar
- Delete profiles
- Profile switching with animations

#### 3.1.3 Video Playback
**How it Works:**
- ExoPlayer (Media3) integration for video playback
- Custom control interface with:
  - Play/Pause functionality
  - 10-second rewind/forward
  - Volume control slider
  - Playback speed adjustment (0.5x, 1x, 1.5x, 2x)
- Full-screen landscape mode
- Resume playback from last position
- Chromecast support for casting to TV

**Screenshot Placeholder**: *[Video Player Screen with Controls]*

**Code Implementation:**
```java
// PlayerActivity.java
private void preparePlayer(Uri uri, Movie movie) {
    MediaItem mediaItem = new MediaItem.Builder()
        .setUri(uri)
        .setMediaMetadata(new MediaMetadata.Builder()
            .setTitle(movie.getTitle()).build())
        .build();
    
    player.setMediaItem(mediaItem);
    player.prepare();
    
    Long pos = playerViewModel.getPlaybackPosition().getValue();
    if (pos != null) player.seekTo(pos);
    player.play();
}
```

#### 3.1.4 Content Discovery
**How it Works:**
- Home screen displays multiple content sections:
  - "Only On Netflix": Featured content
  - "Recommended For You": Personalized recommendations
  - Genre-based sections (Action, Comedy, Drama, etc.)
  - "Watch List": Saved movies
  - "Continue Watching": Resume playback
  - "Favourites": Favorite movies
  - "Because You Watched": Dynamic recommendations based on watch history
- TMDB API integration for movie data
- Real-time updates using LiveData

**Screenshot Placeholder**: *[Home Screen with Multiple Sections]*

#### 3.1.5 Search Functionality
**How it Works:**
- Real-time search as user types
- Voice recognition for hands-free search
- Grid layout display of search results
- Integration with TMDB search API
- Instant results with debouncing

**Screenshot Placeholder**: *[Search Screen with Results]*

**Code Implementation:**
```java
// Search.java
searchInput.addTextChangedListener(new TextWatcher() {
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        searchViewModel.searchMovies(s.toString());
    }
});
```

#### 3.1.6 Offline Downloads
**How it Works:**
- Download videos for offline viewing
- Background download service
- Download progress tracking
- Download management interface
- Offline playback capability

**Screenshot Placeholder**: *[Downloads Screen]*

### 3.2 Additional Features

#### 3.2.1 Interactive Onboarding
- Swipe-based movie preference selection
- Card stack interface with smooth animations
- Real-time recommendation generation
- Automatic navigation after completion

#### 3.2.2 Personalized Recommendations
- "Because You Watched" sections dynamically generated
- Recommendations based on watch history
- Profile-specific recommendations

#### 3.2.3 Watch History Tracking
- Automatic tracking of watched content
- Resume playback from last position
- Continue Watching section on home screen

#### 3.2.4 Favorites and Watch List
- Add movies to favorites
- Save movies to watch list
- Profile-specific lists
- Real-time synchronization with Firebase

#### 3.2.5 Chromecast Support
- Cast videos to Chromecast devices
- Seamless casting experience
- Media route button in player

---

## 4. Innovation & Creativity (10%)

### 4.1 Unique Features or Approaches

#### 4.1.1 Swipe-Based Onboarding System
**Innovation**: Instead of traditional form-based onboarding, we implemented an interactive card stack interface where users swipe through movies to indicate preferences.

**Why it Stands Out:**
- **Engaging User Experience**: Makes onboarding fun and interactive
- **Natural Preference Collection**: Users express preferences through actions rather than forms
- **Real-time Recommendations**: System generates recommendations as users swipe
- **Visual Appeal**: Smooth animations and card stack effects create a modern, polished feel

**Implementation Details:**
- CardStackView library for swipe gestures
- Background thread processing for recommendation generation
- Duplicate filtering to avoid showing same movies
- Automatic navigation after sufficient swipes

#### 4.1.2 Dynamic "Because You Watched" Sections
**Innovation**: The home screen dynamically creates new recommendation sections based on each movie the user watches, creating a personalized and evolving content discovery experience.

**Why it Stands Out:**
- **Personalized Experience**: Each user sees different sections based on their viewing history
- **Dynamic Content**: Sections appear automatically as users watch new content
- **Scalable Design**: Can handle unlimited watched movies without cluttering the UI
- **Smart Recommendations**: Uses TMDB API to find similar content

**Technical Implementation:**
```java
// MainActivity.java
private void loadWatchedMoviesFromFirebase() {
    mainViewModel.getWatchedMoviesFromFirebase().observe(this, watchedTitles -> {
        if (watchedTitles != null && !watchedTitles.isEmpty()) {
            // Find new movies that weren't in the previous list
            List<String> newMovies = new ArrayList<>();
            for (String watchedMovieTitle : watchedTitles) {
                if (!previousWatchedTitles.contains(watchedMovieTitle)) {
                    newMovies.add(watchedMovieTitle);
                }
            }
            // Create "Because You Watched" sections for new movies only
            for (String newMovieTitle : newMovies) {
                addCategorySection(newMovieTitle);
            }
        }
    });
}
```

#### 4.1.3 Shared Element Transitions with ArcMotion
**Innovation**: Implemented smooth ArcMotion transitions for profile selection, creating a visually appealing animation that follows a curved path rather than linear movement.

**Why it Stands Out:**
- **Professional Polish**: ArcMotion creates more natural, eye-catching animations
- **Enhanced UX**: Smooth transitions make the app feel more responsive and modern
- **Postponed Transitions**: Ensures images are loaded before animation starts
- **Consistent Branding**: Creates a cohesive visual experience

**Implementation:**
```java
// MainActivity.java
private Transition makeArcMotionTransition() {
    ArcMotion arcMotion = new ArcMotion();
    arcMotion.setMinimumHorizontalAngle(60f);
    arcMotion.setMinimumVerticalAngle(60f);
    
    ChangeBounds changeBounds = new ChangeBounds();
    changeBounds.setPathMotion(arcMotion);
    changeBounds.setDuration(700);
    changeBounds.setInterpolator(new AccelerateDecelerateInterpolator());
    return changeBounds;
}
```

#### 4.1.4 Voice-Enabled Search
**Innovation**: Integrated Android Speech Recognition API for hands-free movie search, allowing users to search by speaking movie names.

**Why it Stands Out:**
- **Accessibility**: Makes search easier for users who prefer voice input
- **Modern Feature**: Aligns with current trends in voice-enabled applications
- **User Convenience**: Faster than typing, especially for long movie names
- **Unique Implementation**: Not commonly seen in streaming apps

#### 4.1.5 Profile-Specific Data Isolation
**Innovation**: Each profile maintains completely separate watch history, favorites, watch list, and recommendations, creating a true multi-user experience.

**Why it Stands Out:**
- **True Multi-User Support**: Each family member gets personalized experience
- **Data Privacy**: Profiles don't interfere with each other
- **Scalable Architecture**: Easy to add more profiles or features
- **Firebase Integration**: Efficient data structure for profile isolation

---

## 5. Technical Complexity (15%)

### 5.1 Technical Challenges

#### 5.1.1 Challenge: Shared Element Transitions with Image Loading
**Problem**: 
Ensuring smooth shared element transitions when profile images need to be loaded from Firebase Storage or network, preventing jarring animations or blank images during transitions.

**Solution**:
Implemented postponed enter transitions that wait for images to load before starting the animation. Used Glide's RequestListener to notify when images are ready.

**Code Snippet:**
```java
// MainActivity.java
supportPostponeEnterTransition();

Glide.with(this)
    .load(avatarUrl)
    .apply(new RequestOptions().dontAnimate())
    .listener(new RequestListener<Drawable>() {
        @Override
        public boolean onResourceReady(Drawable resource, Object model, 
                Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            supportStartPostponedEnterTransition();
            return false;
        }
    })
    .into(profileIcon);
```

**Complexity Level**: High - Required understanding of Android transition framework, image loading lifecycle, and proper timing coordination.

#### 5.1.2 Challenge: Real-time Data Synchronization with Firebase
**Problem**: 
Keeping UI synchronized with Firebase data changes across multiple screens (Home, Profile Page, Details) while maintaining performance and avoiding unnecessary updates.

**Solution**:
Implemented LiveData with Firebase snapshot listeners, using ViewModels to share data across activities. Added proper lifecycle management to prevent memory leaks.

**Code Snippet:**
```java
// MainViewModel.java
public LiveData<List<Movie>> getMyListMovies() {
    if (myListMovies == null) {
        myListMovies = new MutableLiveData<>();
        loadMyListFromFirebase();
    }
    return myListMovies;
}

private void loadMyListFromFirebase() {
    String profileId = getProfileId();
    if (profileId == null) return;
    
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
        .collection("profiles")
        .document(profileId)
        .collection("myList")
        .addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null) return;
            
            List<Movie> movies = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Movie movie = doc.toObject(Movie.class);
                if (movie != null) movies.add(movie);
            }
            myListMovies.postValue(movies);
        });
}
```

**Complexity Level**: High - Required understanding of Firebase real-time listeners, LiveData patterns, and proper lifecycle management.

#### 5.1.3 Challenge: Dynamic RecyclerView Section Generation
**Problem**: 
Dynamically creating and managing multiple RecyclerView sections on the home screen (genres, "Because You Watched" sections) without causing performance issues or UI glitches.

**Solution**:
Created a map-based system to track RecyclerViews and adapters, only creating new sections when needed, and properly managing view visibility based on data availability.

**Code Snippet:**
```java
// MainActivity.java
private final Map<String, RecyclerView> genreRecyclerViewMap = new HashMap<>();
private final Map<String, MoviesAdapter> genreAdapterMap = new HashMap<>();
private final Map<String, List<Movie>> genreMovieListMap = new HashMap<>();

private void addGenreSection() {
    mainViewModel.getGenreMovie().observe(this, movies -> {
        if (movies != null) {
            for (Map.Entry<String, List<Movie>> entry : movies.entrySet()) {
                String genre = entry.getKey();
                List<Movie> movieObject = entry.getValue();
                
                if (genreRecyclerViewMap.containsKey(genre)) {
                    // Update existing RecyclerView
                    List<Movie> sectionMovieList = genreMovieListMap.get(genre);
                    MoviesAdapter adapter = genreAdapterMap.get(genre);
                    if (sectionMovieList != null && adapter != null) {
                        sectionMovieList.clear();
                        sectionMovieList.addAll(movieObject);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    // Create new genre section
                    // ... create TextView and RecyclerView
                    mainContainer.addView(title);
                    mainContainer.addView(recyclerView);
                }
            }
        }
    });
}
```

**Complexity Level**: Medium-High - Required understanding of dynamic view creation, RecyclerView optimization, and proper data binding.

#### 5.1.4 Challenge: Background Download Management
**Problem**: 
Implementing a robust download system that works in the background, tracks download progress, handles failures, and manages storage efficiently.

**Solution**:
Used ExoPlayer's DownloadManager with a foreground service, implemented DownloadTracker for state monitoring, and created a proper lifecycle-aware download management system.

**Code Snippet:**
```java
// DemoDownloadService.java
public class DemoDownloadService extends DownloadService {
    @Override
    protected DownloadManager getDownloadManager() {
        return DemoUtil.getDownloadManager(this);
    }
    
    @Override
    protected NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    
    @Override
    protected Scheduler getScheduler() {
        return DemoUtil.getDownloadScheduler(this);
    }
}
```

**Complexity Level**: High - Required understanding of Android services, ExoPlayer download system, and proper notification management.

#### 5.1.5 Challenge: Onboarding Swipe with Recommendation Generation
**Problem**: 
Processing user swipes, generating recommendations in real-time, filtering duplicates, and updating the card stack without causing UI freezes or infinite loops.

**Solution**:
Used ExecutorService for background processing, implemented duplicate filtering logic, and carefully managed adapter updates to prevent loops while maintaining smooth UI performance.

**Code Snippet:**
```java
// OnboardingSwipe.java
executorService.execute(() -> {
    if (viewModelSize > adapterSize) {
        List<Movie> newMovies = new ArrayList<>(movies.subList(adapterSize, viewModelSize));
        List<Movie> uniqueNewMovies = filterDuplicates(newMovies, existingMovies);
        
        if (!uniqueNewMovies.isEmpty()) {
            runOnUiThread(() -> {
                adapter.addMovies(uniqueNewMovies);
            });
        }
    }
});

private List<Movie> filterDuplicates(List<Movie> newMovies, List<Movie> existingMovies) {
    Set<String> existingTitles = new HashSet<>();
    for (Movie movie : existingMovies) {
        existingTitles.add(movie.getTitle());
    }
    
    Set<String> seenTitles = new HashSet<>();
    List<Movie> uniqueMovies = new ArrayList<>();
    
    for (Movie newMovie : newMovies) {
        String title = newMovie.getTitle();
        if (!existingTitles.contains(title) && seenTitles.add(title)) {
            uniqueMovies.add(newMovie);
        }
    }
    return uniqueMovies;
}
```

**Complexity Level**: Medium-High - Required understanding of background threading, adapter management, and preventing race conditions.

---

## 6. Security and Data Management (10%)

### 6.1 Data Handling

#### 6.1.1 Data Storage Architecture
**Firebase Firestore**: 
- User authentication data
- User profiles (name, avatar URL, color index)
- Watch history (movie titles, playback positions)
- Favorites list
- Watch list
- Continue watching data

**Firebase Storage**:
- Profile avatar images
- Secure upload and download with proper access rules

**Local Storage**:
- SharedPreferences for profile ID and app preferences
- ExoPlayer DownloadManager for offline video storage
- Encrypted local database for download metadata

**Data Structure Example:**
```
users/
  {userId}/
    profiles/
      {profileId}/
        name: String
        avatarUrl: String
        colorIndex: Int
        myList: [Movie objects]
        watchList: [Movie objects]
        continueWatching: [Movie objects with position]
        watchHistory: [String movie titles]
```

#### 6.1.2 Data Management Practices
- **Profile-Specific Data Isolation**: Each profile's data is stored separately in Firestore collections
- **Real-time Synchronization**: Snapshot listeners ensure UI stays updated with latest data
- **Efficient Queries**: Firestore queries are optimized to fetch only necessary data
- **Offline Support**: Firestore offline persistence for viewing data without internet
- **Data Validation**: Input validation before storing data in Firebase

### 6.2 Security Measures

#### 6.2.1 Authentication Security
**Firebase Authentication**:
- Secure email/password authentication
- Session management handled by Firebase
- Automatic token refresh
- Secure sign-out functionality

**Implementation:**
```java
// LoginViewModel.java
FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            // User authenticated
        } else {
            // Handle authentication failure
        }
    });
```

#### 6.2.2 Data Security
**Firebase Security Rules**:
- User data is only accessible by the authenticated user
- Profile data is isolated per user
- Read/write permissions properly configured

**Input Validation**:
- Email format validation
- Password strength requirements
- Profile name validation (non-empty, length limits)
- Image upload validation (file type, size)

**Code Example:**
```java
// EmailViewModel.java
public boolean onNextClicked(String email) {
    if (email == null || email.trim().isEmpty()) {
        return false;
    }
    
    // Email format validation
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    if (!email.matches(emailPattern)) {
        return false;
    }
    
    navigateToPassword.postValue(email);
    return true;
}
```

#### 6.2.3 Network Security
**HTTPS Only**:
- All API calls use HTTPS
- Network security config enforces secure connections
- TMDB API calls use secure endpoints

**API Key Management**:
- TMDB API key stored in buildConfig (not in code)
- API key not exposed in version control
- Secure API key retrieval

**Network Security Config:**
```xml
<!-- network_security_config.xml -->
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

#### 6.2.4 Secure Data Transmission
- All Firebase communications encrypted (TLS)
- API responses validated before processing
- Error handling prevents data leakage
- Secure file uploads to Firebase Storage

---

## 7. Testing and Debugging (10%)

### 7.1 Testing Strategy

#### 7.1.1 Manual Testing Approach
**Functional Testing**:
- Tested all user flows (login, profile creation, video playback, search, etc.)
- Verified all features work as expected
- Tested edge cases (empty lists, network failures, etc.)

**UI/UX Testing**:
- Tested on multiple screen sizes (phone, tablet)
- Verified animations and transitions
- Tested dark theme consistency
- Checked accessibility features

**Performance Testing**:
- Tested app performance with large movie lists
- Verified smooth scrolling in RecyclerViews
- Tested image loading and caching
- Checked memory usage

**Integration Testing**:
- Tested Firebase integration (auth, Firestore, Storage)
- Verified TMDB API integration
- Tested ExoPlayer functionality
- Verified download system

**Device Testing**:
- Tested on Android 8.0+ (minimum SDK 26)
- Verified on different Android versions
- Tested on emulator and physical devices

#### 7.1.2 Testing Tools Used
- **Android Studio**: Built-in testing framework
- **Logcat**: For debugging and log analysis
- **Firebase Console**: For monitoring Firestore and Storage
- **Postman**: For API testing (TMDB)
- **Android Device Monitor**: For performance monitoring

### 7.2 Debugging Process

#### 7.2.1 Common Bugs Encountered and Resolutions

**Bug 1: Shared Element Transition Not Working**
**Problem**: Profile avatar transition was not smooth, images appeared blank during transition.

**Debugging Process**:
- Added Logcat logs to track image loading
- Discovered images weren't loaded before transition started
- Checked Glide loading callbacks

**Solution**:
```java
// Implemented postponed enter transition
supportPostponeEnterTransition();

Glide.with(this)
    .load(avatarUrl)
    .listener(new RequestListener<Drawable>() {
        @Override
        public boolean onResourceReady(...) {
            supportStartPostponedEnterTransition();
            return false;
        }
    })
    .into(profileIcon);
```

**Logcat Evidence**: *[Screenshot of Logcat showing image load completion]*

---

**Bug 2: Infinite Loop in Onboarding Swipe**
**Problem**: Onboarding adapter was updating infinitely, causing UI freezes.

**Debugging Process**:
- Added extensive logging to track adapter updates
- Discovered ViewModel was emitting same data multiple times
- Found that adapter updates triggered ViewModel updates

**Solution**:
```java
// Added duplicate checking and size comparison
if (viewModelSize > adapterSize) {
    // Only update if new movies added
    List<Movie> newMovies = movies.subList(adapterSize, viewModelSize);
    adapter.addMovies(filterDuplicates(newMovies, existingMovies));
} else if (viewModelSize < adapterSize && adapterSize - viewModelSize > 5) {
    // Only reset if significant reduction
    adapter.setMovies(movies);
}
// Otherwise ignore to prevent loops
```

**Logcat Evidence**: *[Screenshot of Logcat showing adapter update logs]*

---

**Bug 3: Firebase Data Not Updating in Real-time**
**Problem**: Changes made in one screen weren't reflected in other screens immediately.

**Debugging Process**:
- Checked Firebase snapshot listeners
- Verified LiveData observers
- Discovered multiple ViewModel instances

**Solution**:
- Used ViewModelProvider to ensure single ViewModel instance
- Implemented proper LiveData observation
- Added refresh methods for manual updates

**Logcat Evidence**: *[Screenshot of Logcat showing Firebase listener callbacks]*

---

**Bug 4: Download Progress Not Updating**
**Problem**: Download progress wasn't showing in UI, downloads appeared stuck.

**Debugging Process**:
- Checked DownloadTracker implementation
- Verified DownloadManager callbacks
- Discovered UI updates weren't on main thread

**Solution**:
```java
// Ensured UI updates on main thread
downloadTracker.addListener(new DownloadTracker.Listener() {
    @Override
    public void onDownloadsChanged() {
        runOnUiThread(() -> {
            loadDownloads();
            adapter.notifyDataSetChanged();
        });
    }
});
```

**Logcat Evidence**: *[Screenshot of Logcat showing download state changes]*

---

**Bug 5: Memory Leak in Onboarding Activity**
**Problem**: Onboarding activity wasn't releasing resources, causing memory issues.

**Debugging Process**:
- Used Android Profiler to identify memory leak
- Discovered ExecutorService wasn't being shut down
- Found background threads still running after activity destruction

**Solution**:
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (executorService != null && !executorService.isShutdown()) {
        executorService.shutdown();
    }
}
```

**Logcat Evidence**: *[Screenshot of Android Profiler showing memory usage]*

---

### 7.3 Testing Evidence

**Logcat Screenshots**: *[Insert Logcat screenshots showing:*
- *Firebase authentication logs*
- *API call logs*
- *Image loading logs*
- *Download progress logs*
- *Error handling logs*]

**Test Results Summary**:
- ✅ All core features functional
- ✅ UI/UX tested and verified
- ✅ Performance optimized
- ✅ Integration tested
- ✅ Edge cases handled
- ✅ Memory leaks resolved

---

## 8. Team Collaboration (5%)

### 8.1 Contribution Breakdown

#### 8.1.1 Arhaan - UI/UX Developer & Frontend Developer
**Coding Contributions**:
- Login page UI implementation
- Shared element transitions (ArcMotion)
- Swipe-based onboarding UI
- Search functionality with voice recognition
- Profile page UI (edit/delete profiles, signout)
- Onboarding swipe backend logic

**Design Contributions**:
- UI/UX design for login screens
- Animation and transition design
- Profile management interface design
- Search interface design

**Documentation**:
- Code comments for UI components
- Transition implementation documentation

**Testing**:
- UI/UX testing
- Animation testing
- Search functionality testing

#### 8.1.2 Naitik - Backend Developer & Video Player Specialist
**Coding Contributions**:
- ExoPlayer video playing implementation with custom controls
- Download functionality implementation
- Firebase integration (Authentication, Firestore, Storage)
- Favorites, Continue Watching, Watch Later features
- Chromecast functionality
- Home screen backend (recommendations, genre sections)
- ViewModel implementations
- Repository pattern implementation

**Architecture Contributions**:
- MVVM architecture setup
- Firebase data structure design
- API integration (TMDB)
- Background service implementation

**Documentation**:
- Technical documentation
- API integration documentation
- Firebase setup documentation

**Testing**:
- Backend functionality testing
- Firebase integration testing
- Video player testing
- Download system testing

#### 8.1.3 Aanchal - UI/UX Developer & Frontend Developer
**Coding Contributions**:
- Login page UI implementation
- Shared element transitions
- Swipe-based onboarding UI
- Search functionality
- Profile page UI (edit/delete profiles, signout)

**Design Contributions**:
- UI/UX design collaboration
- Animation design
- User experience improvements

**Documentation**:
- UI component documentation
- User flow documentation

**Testing**:
- UI testing
- User experience testing
- Cross-device testing

### 8.2 Tools for Collaboration

#### 8.2.1 Version Control
**GitHub**:
- Repository for code version control
- Branch management for feature development
- Pull requests for code review
- Issue tracking for bugs and features
- Commit history for tracking changes

**Git Workflow**:
- Feature branches for new features
- Main branch for stable code
- Regular commits with descriptive messages
- Code review before merging

#### 8.2.2 Communication
**Regular Meetings**:
- Weekly team meetings to discuss progress
- Feature planning sessions
- Bug triage meetings
- Code review sessions

**Communication Channels**:
- In-person meetings
- WhatsApp for quick updates
- Email for formal communication

#### 8.2.3 Project Management
**Task Distribution**:
- Clear division of responsibilities
- Regular progress updates
- Milestone tracking
- Feature completion tracking

**Code Organization**:
- Consistent coding standards
- Code comments for clarity
- Modular architecture for easy collaboration

---

## 9. Documentation (5%)

### 9.1 Code Documentation
- **Inline Comments**: Code is well-commented with explanations for complex logic
- **Method Documentation**: Key methods have JavaDoc comments
- **Architecture Documentation**: MVVM pattern clearly documented
- **API Documentation**: TMDB API integration documented

### 9.2 Project Documentation
- **README File**: Project setup and build instructions
- **Feature Documentation**: Each feature has implementation details
- **Setup Guide**: Firebase setup and API key configuration
- **This Report**: Comprehensive project documentation

### 9.3 Submission
This document serves as the complete project documentation, covering all aspects of the application development, implementation, and testing.

---

## 10. Demo & Viva (10%)

### 10.1 Demo Preparation

#### 10.1.1 Demo Flow
1. **Splash Screen**: Show app launch and animation
2. **Authentication**: Demonstrate login flow
3. **Onboarding**: Show swipe-based preference selection
4. **Profile Selection**: Demonstrate profile switching with transitions
5. **Home Screen**: Show all content sections and recommendations
6. **Search**: Demonstrate text and voice search
7. **Movie Details**: Show movie information screen
8. **Video Playback**: Demonstrate player with custom controls
9. **Downloads**: Show offline download functionality
10. **Profile Management**: Show profile editing and management

#### 10.1.2 Key Features to Highlight
- Smooth animations and transitions
- Real-time data synchronization
- Personalized recommendations
- Offline viewing capability
- Multi-profile support
- Voice search functionality

### 10.2 Viva Preparation

#### 10.2.1 Technical Questions Preparation
**Architecture**:
- MVVM pattern implementation
- LiveData and ViewModel usage
- Repository pattern

**Firebase**:
- Authentication implementation
- Firestore data structure
- Real-time listeners

**Video Playback**:
- ExoPlayer integration
- Custom controls implementation
- Download system

**UI/UX**:
- Shared element transitions
- Animation implementation
- Material Design principles

#### 10.2.2 Teamwork Questions Preparation
- Individual contributions
- Collaboration process
- Challenges faced and solutions
- Learning outcomes

---

## 11. Conclusion

### 11.1 Development Process Summary
The development of this Netflix clone application was a comprehensive learning experience that involved:
- **Planning Phase**: Feature planning, architecture design, technology selection
- **Development Phase**: Iterative development with regular testing and refinement
- **Integration Phase**: Combining individual contributions into a cohesive application
- **Testing Phase**: Comprehensive testing and bug fixing
- **Documentation Phase**: Creating detailed documentation

### 11.2 Challenges Faced
1. **Technical Challenges**:
   - Implementing smooth shared element transitions
   - Managing real-time Firebase data synchronization
   - Creating dynamic UI sections
   - Implementing background download system
   - Optimizing performance with large datasets

2. **Collaboration Challenges**:
   - Coordinating feature development
   - Merging code from multiple developers
   - Maintaining consistent code style
   - Managing project timeline

3. **Learning Challenges**:
   - Learning new technologies (ExoPlayer, Firebase)
   - Understanding Android architecture components
   - Implementing complex animations
   - Optimizing app performance

### 11.3 Solutions Implemented
- **Architecture**: MVVM pattern for clean code organization
- **Real-time Updates**: LiveData and Firebase snapshot listeners
- **Performance**: Efficient RecyclerView usage, image caching, background threading
- **User Experience**: Smooth animations, intuitive navigation, responsive design
- **Collaboration**: Clear division of responsibilities, regular communication, version control

### 11.4 Project Outcome
The project successfully delivers a fully functional streaming application with:
- ✅ Complete authentication system
- ✅ Multi-profile support
- ✅ Video playback with custom controls
- ✅ Offline download capability
- ✅ Personalized recommendations
- ✅ Search functionality
- ✅ Profile management
- ✅ Smooth UI/UX with animations

### 11.5 Learning Outcomes
**Technical Skills**:
- Android development with Java
- MVVM architecture pattern
- Firebase integration (Auth, Firestore, Storage)
- ExoPlayer for video playback
- Material Design implementation
- API integration (TMDB)
- Background services
- Advanced animations

**Soft Skills**:
- Team collaboration
- Project management
- Problem-solving
- Code review
- Documentation

### 11.6 Future Improvements
**Features**:
- Push notifications
- Social features (sharing, reviews)
- Parental controls
- Multiple language support
- Advanced recommendation algorithms
- Subtitle support
- Picture-in-picture mode

**Technical**:
- Unit testing and UI testing
- Performance optimization
- Code refactoring
- Enhanced error handling
- Analytics integration
- CI/CD pipeline

### 11.7 Reflection
This project provided valuable hands-on experience in Android development, from UI design to backend integration. The challenges faced and solutions implemented have significantly enhanced our understanding of mobile application development. The collaborative nature of the project taught us the importance of clear communication, code organization, and teamwork in software development.

---

## 12. Appendix

### 12.1 Full Code Repository

**GitHub Repository**: 
*[Insert GitHub repository link here]*

**Repository Structure**:
```
Netflix-Clone/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/madminiproject/
│   │   │   │   ├── Activities/
│   │   │   │   ├── ViewModels/
│   │   │   │   ├── Adapters/
│   │   │   │   ├── Models/
│   │   │   │   └── Services/
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   └── values/
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml
```

**Key Files**:
- `MainActivity.java`: Home screen implementation
- `PlayerActivity.java`: Video player implementation
- `MainViewModel.java`: Home screen ViewModel
- `PlayerViewModel.java`: Video player ViewModel
- `OnboardingSwipe.java`: Swipe-based onboarding
- `Search.java`: Search functionality
- `ProfilePageActivity.java`: Profile management
- `DownloadsActivity.java`: Download management

### 12.2 References

#### 12.2.1 Official Documentation
- **Android Developer Documentation**: https://developer.android.com
- **Firebase Documentation**: https://firebase.google.com/docs
- **ExoPlayer Documentation**: https://developer.android.com/guide/topics/media/exoplayer
- **Material Design Guidelines**: https://material.io/design
- **TMDB API Documentation**: https://developers.themoviedb.org/3

#### 12.2.2 Libraries and Frameworks
- **Retrofit**: https://square.github.io/retrofit/
- **Glide**: https://github.com/bumptech/glide
- **CardStackView**: https://github.com/yuyakaido/CardStackView
- **Lottie**: https://lottiefiles.com/
- **Google Cast Framework**: https://developers.google.com/cast/docs/android_sender

#### 12.2.3 Tutorials and Resources
- Android MVVM Architecture Tutorials
- Firebase Authentication Tutorials
- ExoPlayer Implementation Guides
- Material Design Implementation Guides
- Shared Element Transitions Tutorials

#### 12.2.4 Stack Overflow and Community Resources
- Various Stack Overflow threads for specific implementation issues
- Android Developer Community forums
- GitHub repositories for reference implementations

---

**Project Duration**: [Insert project duration]
**Team Members**: Arhaan, Naitik, Aanchal
**Course**: Mobile Application Development (MAD)
**Institution**: NMIMS
**Academic Year**: [Insert academic year]

---

*End of Report*
