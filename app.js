const express = require("express");
const mongoose = require("mongoose");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const cors = require("cors");
const dotenv = require("dotenv");

dotenv.config();
const app = express();

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const authRouter = require("./routes/auth");
const productRouter = require("./routes/products");
const categoryRouter = require("./routes/category");
const orderRouter = require("./routes/order");
const reviewRouter = require("./routes/review");
const memberRouter = require('./routes/user');
const adminRouter = require('./routes/admin');
const statsRouter = require('./routes/stats');
app.use("/api", authRouter);
app.use("/api", productRouter);
app.use("/api", categoryRouter);
app.use("/api", orderRouter);
app.use("/api", reviewRouter);
app.use("/api", memberRouter);
app.use("/api", adminRouter);
app.use("/api", statsRouter);
mongoose.connect(
  process.env.MONGODB_URI || "mongodb://localhost:27017/clothing_store"
);
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
