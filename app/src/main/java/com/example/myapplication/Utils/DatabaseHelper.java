package com.example.myapplication.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.myapplication.Models.User;
import com.example.myapplication.Models.Product;
import com.example.myapplication.Models.Category;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ECommerceDB";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_CART = "cart";
    private static final String TABLE_WISHLIST = "wishlist";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String TABLE_REVIEWS = "reviews";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "id TEXT PRIMARY KEY, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "name TEXT, " +
                "phone TEXT, " +
                "address TEXT, " +
                "role TEXT DEFAULT 'user', " +
                "isActive INTEGER DEFAULT 1)";
        db.execSQL(createUsersTable);

        // Categories table
        String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "imageUrl TEXT)";
        db.execSQL(createCategoriesTable);

        // Products table
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "price REAL NOT NULL, " +
                "imageUrl TEXT, " +
                "categoryId TEXT, " +
                "stock INTEGER DEFAULT 0, " +
                "rating REAL DEFAULT 0, " +
                "reviewCount INTEGER DEFAULT 0, " +
                "FOREIGN KEY(categoryId) REFERENCES " + TABLE_CATEGORIES + "(id))";
        db.execSQL(createProductsTable);

        // Cart table
        String createCartTable = "CREATE TABLE " + TABLE_CART + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId TEXT NOT NULL, " +
                "productId TEXT NOT NULL, " +
                "quantity INTEGER DEFAULT 1, " +
                "FOREIGN KEY(userId) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY(productId) REFERENCES " + TABLE_PRODUCTS + "(id))";
        db.execSQL(createCartTable);

        // Wishlist table
        String createWishlistTable = "CREATE TABLE " + TABLE_WISHLIST + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId TEXT NOT NULL, " +
                "productId TEXT NOT NULL, " +
                "FOREIGN KEY(userId) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY(productId) REFERENCES " + TABLE_PRODUCTS + "(id))";
        db.execSQL(createWishlistTable);

        // Orders table
        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                "id TEXT PRIMARY KEY, " +
                "userId TEXT NOT NULL, " +
                "totalAmount REAL NOT NULL, " +
                "status TEXT DEFAULT 'pending', " +
                "shippingAddress TEXT, " +
                "phone TEXT, " +
                "createdAt TEXT, " +
                "FOREIGN KEY(userId) REFERENCES " + TABLE_USERS + "(id))";
        db.execSQL(createOrdersTable);

        // Order items table
        String createOrderItemsTable = "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "orderId TEXT NOT NULL, " +
                "productId TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "price REAL NOT NULL, " +
                "FOREIGN KEY(orderId) REFERENCES " + TABLE_ORDERS + "(id), " +
                "FOREIGN KEY(productId) REFERENCES " + TABLE_PRODUCTS + "(id))";
        db.execSQL(createOrderItemsTable);

        // Reviews table
        String createReviewsTable = "CREATE TABLE " + TABLE_REVIEWS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId TEXT NOT NULL, " +
                "productId TEXT NOT NULL, " +
                "rating REAL NOT NULL, " +
                "comment TEXT, " +
                "createdAt TEXT, " +
                "FOREIGN KEY(userId) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY(productId) REFERENCES " + TABLE_PRODUCTS + "(id))";
        db.execSQL(createReviewsTable);

        // Insert default admin user
        ContentValues adminUser = new ContentValues();
        adminUser.put("id", "admin001");
        adminUser.put("email", "admin@example.com");
        adminUser.put("password", "admin123");
        adminUser.put("name", "Admin");
        adminUser.put("role", "admin");
        adminUser.put("isActive", 1);
        db.insert(TABLE_USERS, null, adminUser);

        // Insert sample categories
        insertSampleCategories(db);
        // Insert sample products
        insertSampleProducts(db);
    }

    private void insertSampleCategories(SQLiteDatabase db) {
        String[] categories = {"Điện thoại", "Laptop", "Tablet", "Phụ kiện"};
        for (int i = 0; i < categories.length; i++) {
            ContentValues values = new ContentValues();
            values.put("id", "cat" + (i + 1));
            values.put("name", categories[i]);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    private void insertSampleProducts(SQLiteDatabase db) {
        String[][] products = {
                {"prod1", "iPhone 15 Pro", "Điện thoại cao cấp với chip A17 Pro", "25000000", "cat1", "50"},
                {"prod2", "Samsung Galaxy S24", "Điện thoại Android flagship", "22000000", "cat1", "30"},
                {"prod3", "MacBook Pro M3", "Laptop chuyên nghiệp cho công việc", "45000000", "cat2", "20"},
                {"prod4", "Dell XPS 15", "Laptop cao cấp cho designer", "35000000", "cat2", "25"},
                {"prod5", "iPad Pro", "Tablet mạnh mẽ cho sáng tạo", "28000000", "cat3", "15"},
                {"prod6", "Tai nghe AirPods Pro", "Tai nghe không dây chống ồn", "6000000", "cat4", "100"}
        };

        for (String[] product : products) {
            ContentValues values = new ContentValues();
            values.put("id", product[0]);
            values.put("name", product[1]);
            values.put("description", product[2]);
            values.put("price", Double.parseDouble(product[3]));
            values.put("categoryId", product[4]);
            values.put("stock", Integer.parseInt(product[5]));
            values.put("rating", 4.5);
            values.put("reviewCount", 10);
            db.insert(TABLE_PRODUCTS, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WISHLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // User methods
    public boolean registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", user.getId());
        values.put("email", user.getEmail());
        values.put("password", user.getPassword());
        values.put("name", user.getName());
        values.put("phone", user.getPhone());
        values.put("address", user.getAddress());
        values.put("role", user.getRole());
        values.put("isActive", user.isActive() ? 1 : 0);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{"id", "email", "password", "name", "phone", "address", "role", "isActive"},
                "email = ? AND password = ? AND isActive = 1",
                new String[]{email, password},
                null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getString(0));
            user.setEmail(cursor.getString(1));
            user.setPassword(cursor.getString(2));
            user.setName(cursor.getString(3));
            user.setPhone(cursor.getString(4));
            user.setAddress(cursor.getString(5));
            user.setRole(cursor.getString(6));
            user.setActive(cursor.getInt(7) == 1);
        }
        cursor.close();
        db.close();
        return user;
    }

    // Product methods
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getString(0));
                product.setName(cursor.getString(1));
                product.setDescription(cursor.getString(2));
                product.setPrice(cursor.getDouble(3));
                product.setImageUrl(cursor.getString(4));
                product.setCategoryId(cursor.getString(5));
                product.setStock(cursor.getInt(6));
                product.setRating(cursor.getDouble(7));
                product.setReviewCount(cursor.getInt(8));
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return productList;
    }

    public Product getProductById(String productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS,
                null, "id = ?", new String[]{productId},
                null, null, null);

        Product product = null;
        if (cursor.moveToFirst()) {
            product = new Product();
            product.setId(cursor.getString(0));
            product.setName(cursor.getString(1));
            product.setDescription(cursor.getString(2));
            product.setPrice(cursor.getDouble(3));
            product.setImageUrl(cursor.getString(4));
            product.setCategoryId(cursor.getString(5));
            product.setStock(cursor.getInt(6));
            product.setRating(cursor.getDouble(7));
            product.setReviewCount(cursor.getInt(8));
        }
        cursor.close();
        db.close();
        return product;
    }

    // Category methods
    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES, null);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getString(0));
                category.setName(cursor.getString(1));
                category.setImageUrl(cursor.getString(2));
                categoryList.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categoryList;
    }
}

