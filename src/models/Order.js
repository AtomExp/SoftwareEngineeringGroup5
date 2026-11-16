class Order {
  constructor(orderId, items, subtotal, tax, total, createdAt) {
    Object.assign(this, { orderId, items, subtotal, tax, total, createdAt, status: 'CONFIRMED' });
  }
  calculateTotal() { return this.total; }
  confirmOrder() { this.status = 'CONFIRMED'; }
}
module.exports = Order;
