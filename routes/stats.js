const express = require("express");
const router = express.Router();
const User = require("../model/user");
const Product = require("../model/product");
const Order = require("../model/order");
const { authenticateToken, checkAdmin } = require("../middleware/auth");

// lấy thống kê báo cáo
router.get(
  "/api/admin/stats",
  authenticateToken,
  checkAdmin,
  async (req, res) => {
    try {
      const totalUsers = await User.countDocuments();
      const totalProducts = await Product.countDocuments();
      const totalOrders = await Order.countDocuments();
      const totalRevenue = (await Order.aggregate([
        { $group: { _id: null, total: { $sum: "$totalPrice" } } },
      ])[0]) || { total: 0 };

      const ordersByStatus = await Order.aggregate([
        { $group: { _id: "$status", count: { $sum: 1 } } },
      ]);

      const revenueByMonth = await Order.aggregate([
        {
          $group: {
            _id: { $month: "$createdAt" },
            total: { $sum: "$totalPrice" },
          },
        },
        { $sort: { _id: 1 } },
      ]);

      res.json({
        totalUsers,
        totalProducts,
        totalOrders,
        totalRevenue: totalRevenue.total,
        ordersByStatus,
        revenueByMonth,
      });
    } catch (err) {
      res.status(500).json({ message: err.message });
    }
  }
);
// lấy thống kê đơn hàng
router.get("/admin/stats/orders", authenticateToken, checkAdmin, async (req, res) => {
  try {
    const totalOrders = await Order.countDocuments();

   
    const ordersByStatus = await Order.aggregate([
      { $group: { _id: "$status", count: { $sum: 1 } } },
    ]);

   
    const revenueByMonth = await Order.aggregate([
      {
        $group: {
          _id: { $month: "$createdAt" },
          total: { $sum: "$totalPrice" },
          count: { $sum: 1 },
        },
      },
      { $sort: { _id: 1 } },
    ]);

  
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

    const revenueByDay = await Order.aggregate([
      { $match: { createdAt: { $gte: sevenDaysAgo } } },
      {
        $group: {
          _id: { $dateToString: { format: "%Y-%m-%d", date: "$createdAt" } },
          total: { $sum: "$totalPrice" },
          count: { $sum: 1 },
        },
      },
      { $sort: { _id: 1 } },
    ]);

    
    const avgOrderValue = (await Order.aggregate([
      { $group: { _id: null, avg: { $avg: "$totalPrice" } } },
    ])[0]) || { avg: 0 };

   
    const totalRevenue = (await Order.aggregate([
      { $group: { _id: null, total: { $sum: "$totalPrice" } } },
    ])[0]) || { total: 0 };

    res.json({
      success: true,
      data: {
        totalOrders,
        totalRevenue: totalRevenue.total || 0,
        avgOrderValue: avgOrderValue.avg || 0,
        ordersByStatus,
        revenueByMonth,
        revenueByDay,
      },
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});
module.exports = router;
