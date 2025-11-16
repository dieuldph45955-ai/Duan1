const express = require("express");
const router = express.Router();
const fs = require("fs");
const Category = require("../model/category");
const { authenticateToken, checkAdmin } = require("../middleware/auth");

// lấy tất cả danh mục
router.get("/categories", async (req, res) => {
  try {
    const categories = await Category.find();
    res.json(categories);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Thêm danh mục (Admin)
router.post(
  "/addcategories",
  authenticateToken,
  checkAdmin,
  async (req, res) => {
    try {
      const { name, description } = req.body;
      const image = req.file ? `/products/${req.file.filename}` : null;

      if (!name) {
        return res.status(400).json({ message: "name is required" });
      }

      const category = new Category({ name, description, image });
      await category.save();

      res.status(201).json(category);
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);

// Cập nhật danh mục (Admin)
router.put(
  "/categories/:id",
  authenticateToken,
  checkAdmin,
  async (req, res) => {
    try {
      const { name, description } = req.body;

      const updateData = {};
      if (name) updateData.name = name;
      if (description) updateData.description = description;
      if (req.file) updateData.image = `/products/${req.file.filename}`;

      const category = await Category.findByIdAndUpdate(
        req.params.id,
        updateData,
        { new: true }
      );
      if (!category)
        return res.status(404).json({ message: "Category not found" });

      res.json(category);
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);
// xoá danh mục admin
router.delete(
  "/categories/:id",
  authenticateToken,
  checkAdmin,
  async (req, res) => {
    try {
      const category = await Category.findByIdAndDelete(req.params.id);
      if (!category)
        return res.status(404).json({ message: "Category not found" });

      // Xóa file hình ảnh
      if (category.image) {
        const filePath = `uploads${category.image}`;
        if (fs.existsSync(filePath)) {
          fs.unlinkSync(filePath);
        }
      }

      res.json({ message: "Category deleted" });
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);
module.exports = router;
