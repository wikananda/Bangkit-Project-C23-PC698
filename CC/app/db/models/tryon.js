"use strict";
const { Model } = require("sequelize");
module.exports = (sequelize, DataTypes) => {
  class Tryon extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The `models/index` file will call this method automatically.
     */
    static associate(models) {
      // define association here
    }
  }
  Tryon.init(
    {
      user: DataTypes.INTEGER,
      gambarBaju: DataTypes.STRING,
      gambarDiri: DataTypes.STRING,
      gambarHasil: DataTypes.STRING,
    },
    {
      sequelize,
      modelName: "Tryon",
    }
  );
  return Tryon;
};
