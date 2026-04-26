# NurseWearConnect Screen Functionalities & Recommendations

This document provides a detailed breakdown of the screen functionalities within the `:app` module and strategic recommendations for improvement.

## 1. Authentication & Onboarding

### `LoginScreen.kt`
- **Functionality**: 
    - Email/Password authentication.
    - Biometric login (Fingerprint/FaceID) support.
    - "Remember Me" and Password Recovery navigation.
    - Role-based redirection logic (handled in ViewModel).
- **Recommendations**:
    - **Security**: Implement account lockout after multiple failed attempts.
    - **UX**: Add social login (Google/Apple) for faster onboarding.
    - **Validation**: Real-time email format validation before submission.

### `RegisterScreen.kt`
- **Functionality**: User registration with role selection (Student, Professional, Vendor).
- **Recommendations**:
    - **Step-based UI**: Use a Stepper for long forms to avoid user fatigue.
    - **Verification**: Integrate SMS or Email OTP for account activation.

### `OnboardingScreen.kt`
- **Functionality**: Introduction to the platform's value proposition.
- **Recommendations**:
    - **Personalization**: Allow users to select their nursing specialty early to customize the initial catalog view.

---

## 2. Core User Experience (Student/Professional)

### `HomeScreen.kt`
- **Functionality**:
    - Multi-role dashboard (Dynamic content for Admin, Vendor, User).
    - Hero banners for featured products.
    - Search and Category filtering.
    - Quick actions and New Arrivals.
- **Recommendations**:
    - **Performance**: Implement pagination or lazy loading for recommendations to handle large catalogs.
    - **Engagement**: Add a "Style Quiz" or "Size Finder" component for medical scrubs.

### `CatalogScreen.kt` & `ProductDetail`
- **Functionality**: Browsing products with filtering (Size, Color, Gender).
- **Recommendations**:
    - **Visuals**: Add a 360-degree view or "Virtual Try-on" (AR) if resources allow.
    - **Social Proof**: Highlights "Best Seller" or "Most Reviewed" tags more prominently.

### `CartScreen.kt` & `OrdersScreen.kt`
- **Functionality**: Shopping cart management and order tracking.
- **Recommendations**:
    - **Checkout**: Add M-Pesa/Mobile Money integration directly for the Kenyan market (`priceKes`).
    - **Notifications**: Push notifications for every stage of the order lifecycle (Processing -> Dispatched -> Delivered).

---

## 3. Vendor Experience

### `VendorInventoryScreen.kt` / `VendorOrdersScreen.kt`
- **Functionality**: Inventory management and sales tracking.
- **Recommendations**:
    - **Automation**: Low-stock alerts that automatically notify vendors.
    - **Bulk Actions**: Allow bulk price updates or stock replenishment.

### `VendorAnalyticsScreen.kt`
- **Functionality**: Performance metrics.
- **Recommendations**:
    - **Insights**: Provide AI-driven suggestions on which products are trending in specific hospitals/regions.

---

## 4. Administrative Management

### `AdminUserManagementScreen.kt` & `AdminVendorApprovalsScreen.kt`
- **Functionality**: System-wide control and vendor vetting.
- **Recommendations**:
    - **Audit Trail**: View detailed logs of admin actions directly within the user profile.
    - **Verification**: Integrated document viewer for vendor business licenses.

### `UserLogsScreen.kt`
- **Functionality**: System activity monitoring.
- **Recommendations**:
    - **Filtering**: Add advanced filtering by Date Range, Severity Level, and User ID.

---

## 5. Global Technical Recommendations
1. **Offline Capability**: Enhance Room database integration to allow users to browse the catalog and add to cart without an active internet connection.
2. **Dynamic Theming**: Support for Dark Mode and accessibility scaling.
3. **Localization**: While focused on Kenya (`KSh`), consider multi-currency support for future expansion.
4. **Data Sync**: Implement a robust synchronization strategy for real-time updates using Supabase Realtime.
