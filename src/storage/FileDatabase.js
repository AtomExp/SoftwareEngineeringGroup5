const fs = require('node:fs');
const path = require('node:path');
const Menu = require('../models/Menu');
const Order = require('../models/Order');

class FileDatabase {
  constructor(dataDir) {
    this.dataDir = dataDir;
    fs.mkdirSync(dataDir, { recursive: true });
    this.menuFile = path.join(dataDir, 'menu.json');
    this.ordersFile = path.join(dataDir, 'orders.json');
    this.reloadMenu();
    if (!fs.existsSync(this.ordersFile)) fs.writeFileSync(this.ordersFile, '[]\n');
    this.loadOrders();
  }
  reloadMenu() {
    const parsed = JSON.parse(fs.readFileSync(this.menuFile, 'utf8'));
    if (!Array.isArray(parsed.recipes)) throw new Error('menu.json must contain a recipes array');
    const ids = new Set();
    for (const entry of parsed.recipes) {
      if (!Number.isSafeInteger(entry.id) || ids.has(entry.id) ||
          !Number.isSafeInteger(Math.round(entry.price * 100)) || entry.price < 0 ||
          typeof entry.name !== 'string' || !Array.isArray(entry.ingredients) || !Array.isArray(entry.tags)) {
        throw new Error('Invalid or duplicate menu item in menu.json');
      }
      ids.add(entry.id);
    }
    this.menu = new Menu(parsed.recipes);
  }
  getMenu() { return this.menu; }
  loadOrders() {
    const records = JSON.parse(fs.readFileSync(this.ordersFile, 'utf8'));
    if (!Array.isArray(records)) throw new Error('orders.json must be an array');
    this.orders = records;
    this.nextOrderId = records.reduce((max, item) => Math.max(max, item.orderId || 0), 0) + 1;
  }
  getOrder(id) { return this.orders.find(order => order.orderId === id) ?? null; }
  saveOrder(items, subtotal, tax, total) {
    const order = new Order(this.nextOrderId, items, subtotal, tax, total, new Date().toISOString());
    const updated = [...this.orders, order];
    const tmp = this.ordersFile + '.tmp';
    fs.writeFileSync(tmp, JSON.stringify(updated, null, 2) + '\n');
    fs.renameSync(tmp, this.ordersFile);
    this.orders = updated;
    this.nextOrderId++;
    return order;
  }
}
module.exports = FileDatabase;
