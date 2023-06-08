"use strict";
const bcrypt = require("bcrypt");
/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    const password = bcrypt.hashSync("abcde", 5);
    await queryInterface.bulkInsert(
      "Users",
      [
        {
          name: "Nikita",
          email: "nikita@mail.com",
          password: password,
          createdAt: new Date(),
          updatedAt: new Date(),
        },

        {
          name: "Inas",
          email: "Inas@mail.com",
          password: password,
          createdAt: new Date(),
          updatedAt: new Date(),
        },
      ],
      {}
    );
  },

  async down(queryInterface, Sequelize) {
    await queryInterface.bulkDelete("Users", null, {});
  },
};
