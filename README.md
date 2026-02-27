# 🛒 ShopApp — Gaming & PC Hardware E-Commerce

A feature-rich Android e-commerce application for browsing and purchasing gaming hardware — consoles, GPUs, gaming PCs, RAM, and peripherals. Built with **Java** and **Firebase**.

---

## 📋 Table of Contents

- [Features](#features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Firebase Setup](#firebase-setup)
- [Admin Access](#admin-access)
- [Project Structure](#project-structure)
- [Data Models](#data-models)
- [License](#license)

---

## ✨ Features

### Shopping
- **Product Catalog** — Browse 22+ gaming products in a responsive 2-column grid
- **Shopping Cart** — Add/remove items, view quantities & totals, checkout with address validation
- **Wishlist** — Save products for later, move items to cart with one tap
- **Order Management** — Place orders, view order history, cancel pending orders

### User Experience
- **Pull-to-Refresh** — Swipe down to refresh product listings
- **Animated Transitions** — Custom slide and fade animations between screens
- **Bottom Navigation** — Quick access to Home, Cart, Wishlist, and Profile tabs
- **Material Design** — Modern UI with Material cards, buttons, and input fields

### Account & Profile
- **Firebase Authentication** — Secure email/password registration and login
- **Auto-Login** — Persistent sessions across app restarts
- **Shipping Address** — Full address form with 190+ country dropdown, saved to Firebase
- **Recent Orders** — Color-coded order status badges (Pending / Shipped / Delivered / Cancelled)

### Admin Panel
- **Role-Based Access** — Admin users unlock additional controls
- **Admin Orders View** — View and manage all orders across all users
- **Product Seeding** — Auto-populate product catalog or add dummy products

### Offline Support
- **Firebase Offline Persistence** — App remains functional without an internet connection

---

##  Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 17 |
| **Platform** | Android (minSdk 24 / targetSdk 34) |
| **Build System** | Gradle 8.4.0 |
| **Authentication** | Firebase Auth |
| **Database** | Firebase Realtime Database |
| **Analytics** | Firebase Analytics |
| **UI Framework** | Material Design Components 1.11.0 |
| **Layouts** | ConstraintLayout, RecyclerView, SwipeRefreshLayout |

---

## 🏗 Architecture

The app follows a clean activity/fragment architecture with Firebase as the backend:

```
┌─────────────────────────────────────────────┐
│                   UI Layer                   │
│  Activities ─── Fragments ─── Adapters       │
├─────────────────────────────────────────────┤
│                 Data Layer                   │
│  Models ─── Firebase Auth ─── Firebase RTDB  │
└─────────────────────────────────────────────┘
```

### Screens

| Screen | Description |
|--------|-------------|
| **SplashActivity** | Branded 2-second splash, redirects to Login |
| **LoginActivity** | Email/password login with auto-login support |
| **RegisterActivity** | New user registration |
| **MainActivity** | Main hub with bottom navigation (4 fragments) |
| **HomeFragment** | Product grid with swipe-to-refresh |
| **CartFragment** | Cart items, checkout flow with address validation |
| **WishlistFragment** | Saved wishlist products |
| **ProfileFragment** | User info, shipping address, recent orders, admin controls |
| **AdminOrdersActivity** | Admin-only view of all orders |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17**
- A **Firebase** project (see [Firebase Setup](#firebase-setup))
- Android device or emulator running **API 24+** (Android 7.0 Nougat)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/HassanOuadghiri/ShopApp.git
   cd ShopApp
   ```

2. **Set up Firebase** (see section below) and place `google-services.json` in the `app/` directory.

3. **Create the Admin config file**
   Create `app/src/main/java/com/HassanProject/shopapp/AdminConfig.java`:
   ```java
   package com.HassanProject.shopapp;

   public class AdminConfig {
       public static final String ADMIN_USERNAME = "your_admin_username";
       public static final String ADMIN_PASSWORD = "your_admin_password";
       public static final String ADMIN_EMAIL = "your_admin_email@example.com";
   }
   ```

4. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or simply open the project in Android Studio and click **Run**.

---

## 🔥 Firebase Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Add an **Android app** with package name `com.HassanProject.shopapp`.
3. Download the `google-services.json` file and place it in the `app/` directory.
4. Enable the following Firebase services:

   | Service | Configuration |
   |---------|--------------|
   | **Authentication** | Enable **Email/Password** sign-in method |
   | **Realtime Database** | Create a database (Europe West 1 region recommended) |

5. Set Realtime Database rules (for development):
   ```json
   {
     "rules": {
       ".read": "auth != null",
       ".write": "auth != null"
     }
   }
   ```

> ⚠️ For production, configure more restrictive security rules.

---

## 🔐 Admin Access

Admin functionality is gated behind an `AdminConfig` class (gitignored for security). Admin users can:

- View all orders from all users
- Add products to the catalog
- Access admin-only menu items

The admin role is stored as a boolean flag (`isAdmin`) on the user record in Firebase Realtime Database.

---

## 📁 Project Structure

```
app/src/main/
├── java/com/HassanProject/shopapp/
│   ├── ShopApplication.java          # Application class (Firebase offline persistence)
│   ├── AdminConfig.java              # Admin credentials (gitignored)
│   │
│   ├── SplashActivity.java           # Splash screen
│   ├── LoginActivity.java            # Login screen
│   ├── RegisterActivity.java         # Registration screen
│   ├── MainActivity.java             # Main hub with bottom navigation
│   ├── ProductListActivity.java      # Alternative product grid view
│   ├── CartActivity.java             # Standalone cart view
│   ├── WishlistActivity.java         # Standalone wishlist view
│   ├── AdminOrdersActivity.java      # Admin orders management
│   │
│   ├── HomeFragment.java             # Product catalog fragment
│   ├── CartFragment.java             # Cart fragment
│   ├── WishlistFragment.java         # Wishlist fragment
│   ├── ProfileFragment.java          # Profile & settings fragment
│   │
│   ├── ProductAdapter.java           # Product grid adapter
│   ├── CartAdapter.java              # Cart list adapter
│   ├── WishlistAdapter.java          # Wishlist adapter
│   ├── OrderAdapter.java             # Admin order list adapter
│   ├── RecentOrderAdapter.java       # User's recent orders adapter
│   │
│   ├── User.java                     # User model
│   ├── Product.java                  # Product model
│   ├── CartItem.java                 # Cart item model
│   ├── Order.java                    # Order model
│   │
│   └── ProductImageHelper.java       # Maps product names → asset images
│
├── assets/
│   └── products/                     # 22 product images (JPGs) — consoles, GPUs, RAM, etc.
│
├── res/
│   ├── layout/                       # 16 XML layouts (activities, fragments, items)
│   ├── menu/                         # Bottom nav & options menus
│   ├── anim/                         # 6 transition animations
│   ├── drawable/                     # App logo, vector icons, status badge
│   ├── values/                       # Colors, strings, themes
│   └── mipmap/                       # App launcher icons
│
└── AndroidManifest.xml
```

---

## 📊 Data Models

### User
| Field | Type | Description |
|-------|------|-------------|
| `uid` | String | Firebase Auth UID |
| `email` | String | User email address |
| `isAdmin` | boolean | Admin role flag |
| `cart` | Map\<String, CartItem\> | User's cart items |
| `wishlist` | Map\<String, Boolean\> | User's wishlisted product IDs |

### Product
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Unique product ID |
| `name` | String | Product name |
| `price` | double | Price in currency |
| `description` | String | Product description |
| `imageUrl` | String | Image filename (loaded from `assets/products/`) |

### CartItem
| Field | Type | Description |
|-------|------|-------------|
| `productId` | String | Reference to Product |
| `productName` | String | Product display name |
| `price` | double | Item price |
| `quantity` | int | Quantity in cart |
| `imageUrl` | String | Image identifier |

### Order
| Field | Type | Description |
|-------|------|-------------|
| `orderId` | String | Unique order ID |
| `userId` | String | Ordering user's UID |
| `userEmail` | String | Ordering user's email |
| `items` | List\<CartItem\> | Ordered items |
| `totalPrice` | double | Total order price |
| `timestamp` | long | Order creation timestamp |
| `status` | String | `PENDING` · `SHIPPED` · `DELIVERED` · `CANCELLED` |

---

## 🔧 Firebase Database Structure

```
shopapp-db/
├── products/
│   └── {productId}/
│       ├── name
│       ├── price
│       ├── description
│       └── imageUrl
├── users/
│   └── {uid}/
│       ├── email
│       ├── isAdmin
│       ├── cart/
│       │   └── {productId}/
│       ├── wishlist/
│       │   └── {productId}: true
│       ├── shippingAddress/
│       │   ├── name, street, city, postalCode
│       │   ├── state, country, phone
│       └── orders/
│           └── {orderId}/
└── orders/
    └── {orderId}/
        ├── userId, userEmail
        ├── items[], totalPrice
        ├── timestamp, status
```

---

## 📄 License

This project is for educational purposes.

---

<p align="center">
  Built with ☕ Java &amp; 🔥 Firebase
</p>
