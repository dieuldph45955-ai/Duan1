const express = require("express");
const router = express.Router();
const User = require("../model/user");
const authenticateToken = require("../middleware/auth");
// lấy tất cả tài khoản admin
router.get("/admin/users", authenticateToken, checkAdmin, async (req, res) => {
  try {
    const users = await User.find().select("-password");
    res.json(users);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});
// khoá/ mở tài khoản
app.put(
  "/admin/users/:id/status",
  authenticateToken,
  checkAdmin,
  async (req, res) => {
    try {
      const { isActive } = req.body;
      const user = await User.findByIdAndUpdate(
        req.params.id,
        { isActive },
        { new: true }
      ).select("-password");
      if (!user) return res.status(404).json({ message: "User not found" });

      res.json(user);
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);
module.exports = router;
