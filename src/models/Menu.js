const MenuItem = require('./MenuItem');
class Menu {
  constructor(items = []) { this.items = items.map(item => new MenuItem(item)); }
  getItems() { return this.items; }
  findItem(id) { return this.items.find(item => item.id === id) ?? null; }
}
module.exports = Menu;
