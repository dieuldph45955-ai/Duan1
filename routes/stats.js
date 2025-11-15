const User = require("../model/user");
const Product = require("../model/product");
const Order = require("../model/order");

// lấy thống kê báo cáo
router.get("/api/admin/stats", authenticateToken, checkAdmin, async (req, res) => {
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
});

module.exports = router;
