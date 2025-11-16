const express = require("express");
const router = express.Router();
const Order = require("../model/order");
const Product = require("../model/product");
const { authenticateToken, checkAdmin } = require("../middleware/auth");

// thêm đơn hàng

router.post("/orders", authenticateToken, async (req, res) => {
  try {
    const { items, shippingAddress, phone, paymentMethod } = req.body;
    let totalPrice = 0;

    for (let item of items) {
      const product = await Product.findById(item.product);
      if (!product)
        return res.status(404).json({ message: "Product not found" });
      totalPrice += product.price * item.quantity;
    }

    const order = new Order({
      user: req.user.id,
      items,
      totalPrice,
      shippingAddress,
      phone,
      paymentMethod,
    });

    await order.save();
    res.status(201).json(order);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});
// Lấy đơn hàng của user
router.get("/orders/my-orders", authenticateToken, async (req, res) => {
  try {
    const orders = await Order.find({ user: req.user.id })
      .populate("items.product")
      .sort({ createdAt: -1 });
    res.json(orders);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// lấy tất cả đơn hàng (admin)
router.get("/ordersAdmin", authenticateToken, checkAdmin, async (req, res) => {
  try {
    const orders = await Order.find()
      .populate("user")
      .populate("items.product")
      .sort({ createdAt: -1 });
    res.json(orders);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});
// Cập nhật trạng thái đơn hàng (Admin)
router.put(
  "/orders/:id/status",
  authenticateToken,
  checkAdmin,
  async (req, res) => {
    try {
      const { status } = req.body;
      const order = await Order.findByIdAndUpdate(
        req.params.id,
        { status },
        { new: true }
      );
      if (!order) return res.status(404).json({ message: "Order not found" });

      res.json(order);
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);

module.exports = router;
