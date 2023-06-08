const bcrypt = require("bcrypt");
const { User } = require("../../db/models");
const jwt = require("jsonwebtoken");

module.exports = {
  signin: async (req, res, next) => {
    try {
      const { email, password } = req.body;
      const checkUser = await User.findOne({ where: { email: email } });
      //   Cek email apakah email yang dimasukkan sama dengan yang ada di database
      if (checkUser) {
        const checkPassword = bcrypt.compareSync(password, checkUser.password);

        // cek password apakah yang dimasukkan sama dengan yang ada di database
        if (checkPassword) {
          const token = jwt.sign(
            {
              user: {
                id: checkUser.id,
                name: checkUser.name,
                email: checkUser.email,
              },
            },
            "secret"
          );
          res.status(200).json({ message: "Success SignIn", data: token });
        } else {
          // pesan jika password salah
          res.status(404).json({ message: "Invalid password" });
        }
      } else {
        // pesan jika email salah diinput
        res.status(403).json({ message: "Invalid Email" });
      }
    } catch (error) {
      console.log(error);
      next(error);
    }
  },

  signup: async (req, res, next) => {
    try {
      const { name, email, password, confirmPassword } = req.body;

      // validasi password dari form dengan password yang diinput pada form checkpassword
      if (password !== confirmPassword) {
        res
          .status(403)
          .json({ message: "Password and confirm password doesn't match" });
      }

      const checkEmail = await User.findOne({ where: { email: email } });
      if (checkEmail) {
        return res.status(403).json({ message: "Email Registered" });
      }

      const user = await User.create({
        name,
        email,
        password: bcrypt.hashSync(password, 5),
        role: "admin",
      });
      //   console.log(user);

      delete user.dataValues.password;

      res.status(201).json({
        message: "Success SignUp",
        data: user,
      });
    } catch (error) {
      next(error);
    }
  },
};
