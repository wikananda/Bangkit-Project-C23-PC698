const { Recommend } = require("../../db/models");

module.exports = {
  recommend: async (req, res) => {
    try {
      //   const {} = req.body;

      const recommend = await Recommend.create({
        gambarBajuRecomm: `images/${req.file.filename}`,
      });

      res.status(201).json({
        message: "Success add Recommend",
        data: recommend,
      });
    } catch (error) {
      console.log(error);
    }
  },
};
