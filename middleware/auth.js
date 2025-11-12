const jwt = require("jsonwebtoken");
const authenticateToken = (req, res, next) => {
  const token = req.headers['authorization']?.split(' ')[1];
  if (!token) return res.status(401).json({ message: 'Token required' });
  
  jwt.verify(token, process.env.JWT_SECRET || 'TOKEN_SECRET', (err, user) => {
    if (err) return res.status(403).json({ message: 'Invalid token' });
    req.user = user;
    next();
  });
};

const checkAdmin = (req, res, next) => {
  if (req.user?.isAdmin) next();
  else res.status(403).json({ message: 'Admin access required' });
};
module.exports = { authenticateToken, checkAdmin };