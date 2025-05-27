# 🕌 Imani App - Islamic Companion for Uganda

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Build Status](https://img.shields.io/badge/Build-Passing-success.svg)](https://github.com)

**Imani App** is a comprehensive Islamic companion application designed specifically for the Ugandan Muslim community. The app provides essential Islamic tools including prayer times, Qibla direction, Quran reader, Du'a generator, and mosque finder - all optimized for the Ugandan context.

## 📱 Features

### 🕐 Prayer Times (Salah)
- **Real-time GPS location** for accurate prayer calculations
- **Aladhan API integration** with multiple calculation methods
- **Arabic prayer names** with countdown timers
- **Offline caching** for 30 days of prayer times
- **Notification support** for prayer reminders

### 🧭 Qibla Direction (القبلة)
- **Mathematical precision** using Haversine formula
- **Animated compass** with smooth rotation
- **Distance to Mecca** calculation
- **Magnetic declination** compensation
- **Real-time orientation** updates

### 📖 Quran Reader (القرآن الكريم)
- **Complete 114 Surahs** with Al-Quran Cloud API
- **Arabic text** with English translations
- **Bookmark system** for favorite verses
- **Search functionality** across all Surahs
- **Reading progress tracking**
- **Adjustable font sizes**

### 🤲 Du'a Generator (الدعاء)
- **Authentic Hisn al-Muslim** content
- **Categorized collections**: Morning/Evening Adhkar, Prayer, Travel
- **AI-ready framework** for personalized Du'a generation
- **Favorites system** with Islamic disclaimer
- **Arabic text** with transliterations

### 🕌 Mosque Finder (المساجد)
- **Multi-source data** aggregation (Google Places + Supabase + Local)
- **Interactive map/list** views with filtering
- **Community contribution** system
- **Advanced search** with radius selection
- **Mosque details** and contact information

## 🏗️ Technical Architecture

### **Modern Android Development**
- **MVVM Architecture** with Jetpack Compose
- **Hilt Dependency Injection** for clean code structure
- **Room Database** for offline data storage
- **Retrofit** for API networking
- **Kotlin Coroutines** for asynchronous operations

### **Backend Integration**
- **Supabase** for user data and community features
- **Google Places API** for mosque location data
- **Aladhan API** for prayer time calculations
- **Al-Quran Cloud API** for Quran content

### **UI/UX Design**
- **Material Design 3** with Islamic theming
- **Green and gold color palette** reflecting Islamic aesthetics
- **RTL support** for Arabic text
- **Responsive design** for various screen sizes

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Arctic Fox or later
- **Android SDK** API 24 (Android 7.0) or higher
- **Kotlin** 2.0.21 or later
- **Google Maps API Key**
- **Supabase Project** credentials

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/imani-app.git
   cd imani-app
   ```

2. **Configure API Keys**
   Create a `local.properties` file in the root directory:
   ```properties
   MAPS_API_KEY=your_google_maps_api_key
   SUPABASE_URL=your_supabase_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   ```

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 📁 Project Structure

```
app/src/main/java/com/imaniapp/uganda/
├── data/                    # Data layer
│   ├── local/              # Room database, DAOs, entities
│   ├── remote/             # API services and DTOs
│   └── repository/         # Repository implementations
├── domain/                  # Domain layer
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Business logic use cases
├── presentation/            # UI layer
│   ├── screens/            # Compose screens
│   ├── viewmodel/          # ViewModels
│   ├── theme/              # UI theming
│   └── utils/              # UI utilities
└── di/                     # Dependency injection modules
```

## 🔧 Configuration

### API Keys Required
1. **Google Maps API Key** - For mosque finder and location services
2. **Supabase URL & Anon Key** - For backend services and community features

### Permissions
The app requires the following permissions:
- `ACCESS_FINE_LOCATION` - For prayer times and Qibla direction
- `ACCESS_COARSE_LOCATION` - For approximate location services
- `INTERNET` - For API calls and data synchronization

## 🧪 Testing

### Manual Testing Checklist
- [ ] Prayer times display correctly for current location
- [ ] Qibla compass points accurately toward Mecca
- [ ] Quran reader loads all Surahs with proper Arabic text
- [ ] Du'a categories display authentic content
- [ ] Mosque finder shows nearby mosques with accurate data

### Automated Testing
- **Unit Tests**: Repository and ViewModel logic
- **Integration Tests**: API and database interactions
- **UI Tests**: Compose screen functionality

## 🌍 Localization

### Supported Languages
- **English** (Primary interface)
- **Arabic** (Religious content)
- **Luganda** (Planned)
- **Swahili** (Planned)

### RTL Support
Full right-to-left text support for Arabic content with proper text alignment and layout direction.

## 📊 Performance

### Optimization Features
- **Offline-first architecture** for core Islamic features
- **Efficient caching** for prayer times and Quran content
- **Lazy loading** for large datasets
- **Memory optimization** for smooth performance

### Supported Devices
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 35 (Android 15)
- **RAM**: 2GB minimum, 4GB recommended
- **Storage**: 100MB for app + cached content

## 🤝 Contributing

### Islamic Content Guidelines
- All religious content must be from **authentic sources**
- **Hisn al-Muslim** for Du'a content
- **Sahih translations** for Quran text
- **Scholarly verification** for prayer time calculations

### Development Guidelines
1. Follow **MVVM architecture** patterns
2. Use **Jetpack Compose** for UI development
3. Implement **proper error handling**
4. Add **comprehensive tests** for new features
5. Maintain **Islamic authenticity** in all content

### Pull Request Process
1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Verify Islamic content authenticity
5. Submit pull request with detailed description

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

### Islamic Sources
- **Hisn al-Muslim** - Authentic Du'a collection
- **Al-Quran Cloud** - Quran API service
- **Aladhan** - Prayer time calculation service
- **Islamic Society of North America (ISNA)** - Prayer time methodology

### Technical Contributors
- **Android Development Team** - Core application development
- **Islamic Scholars** - Content verification and guidance
- **Ugandan Muslim Community** - Requirements and feedback
- **Open Source Community** - Libraries and tools

### Special Thanks
- **Ugandan Muslim Community** for guidance and requirements
- **Islamic Scholars** for content verification
- **Beta Testers** for valuable feedback
- **Open Source Contributors** for amazing libraries

## 📞 Support

### Community Support
- **GitHub Issues** - Bug reports and feature requests
- **Community Forum** - General discussions and help
- **Islamic Content** - Religious advisory board

### Contact Information
- **Technical Issues**: [GitHub Issues](https://github.com/yourusername/imani-app/issues)
- **Islamic Content**: Religious Advisory Board
- **General Inquiries**: community@imaniapp.uganda

## 🔮 Roadmap

### Version 1.1 (Planned)
- [ ] **Audio Quran** recitation with multiple Qaris
- [ ] **Push notifications** for prayer times
- [ ] **AI-powered Du'a** generation with OpenAI/Gemini
- [ ] **Prayer tracking** and consistency analytics

### Version 1.2 (Future)
- [ ] **Islamic calendar** integration
- [ ] **Mosque reviews** and community ratings
- [ ] **Multi-language** support (Luganda, Swahili)
- [ ] **Social features** for community engagement

### Long-term Vision
- [ ] **Expansion** to other East African countries
- [ ] **Advanced analytics** for prayer consistency
- [ ] **Educational content** and Islamic learning modules
- [ ] **Community events** and mosque announcements

---

**Made with ❤️ for the Ugandan Muslim Community**

*"And whoever relies upon Allah - then He is sufficient for him. Indeed, Allah will accomplish His purpose."* - **Quran 65:3** 