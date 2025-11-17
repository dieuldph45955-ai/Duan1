const express = require("express");
const router = express.Router();
const fs = require("fs");
const Category = require("../model/category");
const { authenticateToken, checkAdmin } = require("../middleware/auth");
const upload = require("../middleware/upload");

// lấy tất cả danh mục
router.get("/categories", async (req, res) => {
  try {
    const categories = await Category.find();
    res.json(categories);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;
