class MenuItem {
  constructor(data) { Object.assign(this, data); }
  getPrice() { return this.price; }
  getCustomizations() { return this.ingredients; }
  toJSON() { return {...this}; }
}
module.exports = MenuItem;
