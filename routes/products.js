const express = require("express");
const router = express.Router();
const Product = require("../model/product");
const { authenticateToken, checkAdmin } = require("../middleware/auth");
const upload = require("../middleware/upload");
// Lấy tất cả sản phẩm (tìm kiếm + lọc)
router.get("/getAllProducts", async (req, res) => {
  try {
    const { category, search } = req.query;

    const filter = {};

    // Lọc theo danh mục
    if (category) {
      filter.category = category;
    }

    // Tìm kiếm theo tên sản phẩm
    if (search) {
      filter.name = { $regex: search, $options: "i" }; 
    }

    const products = await Product.find(filter)
      .populate("category")
      .populate("reviews")
      .sort({ createdAt: -1 });

    res.json(products);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Lấy chi tiết sản phẩm
router.get("/products/:id", async (req, res) => {
  try {
    const product = await Product.findById(req.params.id)
      .populate("category")
      .populate("reviews");
    if (!product) return res.status(404).json({ message: "Product not found" });
    res.json(product);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});
// Thêm sản phẩm (Admin)
router.post(
  "/products",
  authenticateToken,
  checkAdmin,
  upload.array("images"),
  async (req, res) => {
    try {
      const { name, description, price, category, sizes } = req.body;
      const imagePaths = req.files.map((file) => `/uploads/${file.filename}`);

      const product = new Product({
        name,
        description,
        price,
        category,
        sizes,
        images: imagePaths,
      });

      await product.save();
      res.status(201).json(product);
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);
// update(admin)
router.put(
  "/products/:id",
  authenticateToken,
  checkAdmin,
  upload.array("images", 5),
  async (req, res) => {
    try {
      const updateData = req.body;

      if (req.files && req.files.length > 0) {
        updateData.images = req.files.map(
          (file) => `/uploads/${file.filename}`
        );
      }

      const product = await Product.findByIdAndUpdate(
        req.params.id,
        updateData,
        { new: true }
      );

      if (!product)
        return res.status(404).json({ message: "Product not found" });
      res.json(product);
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);
// xoá(admin)
router.delete(
  "/products/:id",
  authenticateToken,
  checkAdmin,
  async (req, res) => {
    try {
      const product = await Product.findByIdAndDelete(req.params.id);
      if (!product)
        return res.status(404).json({ message: "Product not found" });
      res.json({ message: "Product deleted" });
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);
module.exports = router;
