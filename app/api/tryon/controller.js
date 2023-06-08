const { Tryon } = require("../../db/models");
module.exports = {
  tryon: async (req, res) => {
    try {
      console.log("Files => ", req.files);
      //   const {} = req.body;

      const tryon = await Tryon.create({
        user: req.user.id,
        gambarBaju: `${req.files.gambarBaju[0].filename}`,
        gambarDiri: `${req.files.gambarDiri[0].filename}`,
      });

      res.status(201).json({
        message: "Success add Tryon",
        data: tryon,
      });
    } catch (error) {
      console.log(error);
    }
  },
};
