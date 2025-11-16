const express = require("express");
const router = express.Router();
const Review = require("../model/Review");
const Product = require("../model/product");
const { authenticateToken } = require("../middleware/auth");
// thêm review
router.post("/reviews", authenticateToken, async (req, res) => {
  try {
    const { product, rating, comment } = req.body;

    if (!product || !rating) {
      return res
        .status(400)
        .json({ message: "product and rating are required" });
    }

    const review = new Review({ user: req.user.id, product, rating, comment });
    await review.save();

    const product_data = await Product.findById(product);
    const avg_rating =
      (product_data.rating * product_data.reviews.length + rating) /
      (product_data.reviews.length + 1);
    await Product.findByIdAndUpdate(product, {
      rating: avg_rating,
      $push: { reviews: review._id },
    });

    res.status(201).json(review);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});
module.exports = router;
