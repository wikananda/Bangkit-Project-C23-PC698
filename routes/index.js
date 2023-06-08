var express = require("express");
var router = express.Router();
const { signin, signup } = require("../app/api/user/controller");
const { tryon } = require("../app/api/tryon/controller");
const { upload } = require("../app/middleware/multer");
const { auth } = require("../app/middleware/auth");

router.post("/tryon", auth, upload, tryon);
router.post("/signin", signin);
router.post("/signup", signup);

module.exports = router;
