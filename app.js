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
app.use("/api", authRouter);
app.use("/api", productRouter);
mongoose.connect(
  process.env.MONGODB_URI || "mongodb://localhost:27017/clothing_store"
);
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
