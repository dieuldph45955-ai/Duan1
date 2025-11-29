const express = require("express");
const router = express.Router();
const Order = require("../model/order");
const Product = require("../model/product");
const { authenticateToken, checkAdmin } = require("../middleware/auth");

// Create order: only allow creating an order when payment is completed/successful.
// We take a price snapshot from Product documents and DO NOT modify Product collection here.
router.post("/orders", authenticateToken, async (req, res) => {
  try {
    const { items, shippingAddress, phone, paymentMethod, paymentResult, paid } = req.body;

    if (!Array.isArray(items) || items.length === 0) {
      return res.status(400).json({ message: "Order items required" });
    }

    // Determine whether payment succeeded. Accept a boolean 'paid' or a paymentResult.status === 'success'
    const paymentSucceeded = paid === true || (paymentResult && String(paymentResult.status).toLowerCase() === "success");

    if (!paymentSucceeded) {
      return res.status(400).json({ message: "Payment not completed. Orders are created only after successful payment." });
    }

    let totalPrice = 0;
    const orderItems = [];

    // Validate products and take price snapshot
    for (let item of items) {
      if (!item.product) return res.status(400).json({ message: "Each item must include product id" });
      const product = await Product.findById(item.product).select("price name");
      if (!product) return res.status(404).json({ message: `Product not found: ${item.product}` });

      const quantity = Number(item.quantity) || 1;
      const priceSnapshot = product.price || 0;
      totalPrice += priceSnapshot * quantity;

      orderItems.push({
        product: item.product,
        quantity,
        price: priceSnapshot,
        name: product.name
      });
    }

    const order = new Order({
      user: req.user.id,
      items: orderItems,
      totalPrice,
      shippingAddress,
      phone,
      paymentMethod,
      paymentResult: paymentResult || { status: "success" },
      status: "Paid"
    });

    await order.save();
    res.status(201).json(order);
  } catch (err) {
    console.error("Create order error:", err);
    res.status(500).json({ message: err.message });
  }
});

// Get orders for current user (history)
router.get("/orders/my-orders", authenticateToken, async (req, res) => {
  try {
    const orders = await Order.find({ user: req.user.id })
      .populate("items.product", "name images price")
      .sort({ createdAt: -1 });
    res.json(orders);
  } catch (err) {
    console.error("Fetch my orders error:", err);
    res.status(500).json({ message: err.message });
  }
});

// Admin: get all orders
router.get("/ordersAdmin", authenticateToken, checkAdmin, async (req, res) => {
  try {
    const orders = await Order.find()
      .populate("user", "fullName email")
      .populate("items.product", "name images price")
      .sort({ createdAt: -1 });
    res.json(orders);
  } catch (err) {
    console.error("Fetch admin orders error:", err);
    res.status(500).json({ message: err.message });
  }
});

// Update order status (admin)
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
      console.error("Update order status error:", err);
      res.status(500).json({ message: err.message });
    }
  }
);

module.exports = router;

