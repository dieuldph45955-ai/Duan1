const express = require("express");
const router = express.Router();
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const User = require("../model/user");
// đăng ký
router.post("/register", async (req, res) => {
  try {
    const { username, email, password,isAdmin } = req.body;

    if (await User.findOne({ email })) {
      return res.status(400).json({ message: "Email already exists" });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const user = new User({
      username,
      email,
      password: hashedPassword,
      isAdmin,
    });
    await user.save();

    const token = jwt.sign(
      { id: user._id, isAdmin: user.isAdmin },
      process.env.JWT_SECRET || "TOKEN_SECRET"
    );
    res.status(201).json({
      message: "User registered",
      token,
      user: { id: user._id, username, email },
    });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// đăng nhập
router.post("/login", async (req, res) => {
  try {
    const { email, password } = req.body;
    const user = await User.findOne({ email });

    if (!user || !(await bcrypt.compare(password, user.password))) {
      return res.status(401).json({ message: "Invalid credentials" });
    }

    if (!user.isActive) {
      return res.status(403).json({ message: "Account is locked" });
    }

    const token = jwt.sign(
      { id: user._id, isAdmin: user.isAdmin },
      process.env.JWT_SECRET || "TOKEN_SECRET"
    );
    res.json({
      token,
      user: {
        id: user._id,
        username: user.username,
        email,
        isAdmin: user.isAdmin,
      },
    });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// quên mật khẩu
router.post("/forgot-password", async (req, res) => {
  try {
    const { email } = req.body;
    const user = await User.findOne({ email });

    if (!user) return res.status(404).json({ message: "User not found" });

    // Tạo token reset (thực tế nên lưu vào DB và gửi email)
    const resetToken = jwt.sign(
      { id: user._id },
      process.env.JWT_SECRET || "TOKEN_SECRET",
      { expiresIn: "1h" }
    );
    res.json({ message: "Reset token sent", resetToken });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});
module.exports = router;
