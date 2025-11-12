const express = require("express");
const router = express.Router();
const User = require("../model/user");
const authenticateToken = require("../middleware/auth");

// lấy thông tin user
router.get("/users/profile", authenticateToken, async (req, res) => {
  try {
    const user = await User.findById(req.user.id).select("-password");
    res.json(user);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});
// Cập nhật thông tin cá nhân user
router.put("/users/profile", authenticateToken, async (req, res) => {
  try {
    const { fullName, phone, address } = req.body;

    const updateData = {};
    if (fullName) updateData.fullName = fullName;
    if (phone) updateData.phone = phone;
    if (address) updateData.address = address;

    const user = await User.findByIdAndUpdate(req.user.id, updateData, {
      new: true,
    }).select("-password");
    res.json(user);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// thêm sản phẩm vào yêu thích (user)
router.post(
  "/users/wishlist/:productId",
  authenticateToken,
  async (req, res) => {
    try {
      const user = await User.findByIdAndUpdate(
        req.user.id,
        { $addToSet: { wishlist: req.params.productId } },
        { new: true }
      ).populate("wishlist");
      res.json(user);
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);
// xoá sản phẩm yêu thích
router.delete(
  "/api/users/wishlist/:productId",
  authenticateToken,
  async (req, res) => {
    try {
      const user = await User.findByIdAndUpdate(
        req.user.id,
        { $pull: { wishlist: req.params.productId } },
        { new: true }
      ).populate("wishlist");
      res.json(user);
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);
module.exports = router;
